#!/usr/bin/env python3
"""Inspect licenses, or apply UaRD's default data license through the native API."""
import argparse
import json
import os
from pathlib import Path
import sys
from urllib.error import HTTPError, URLError
from urllib.parse import urlsplit
from urllib.request import build_opener, HTTPRedirectHandler, Request

ROOT = Path(__file__).resolve().parents[2]
CC_BY_URI = "https://creativecommons.org/licenses/by/4.0"


class NoRedirect(HTTPRedirectHandler):
    def redirect_request(self, req, fp, code, msg, headers, newurl):
        return None


class Client:
    def __init__(self, url, token):
        parsed = urlsplit(url)
        if (parsed.scheme not in {"http", "https"} or not parsed.hostname
                or parsed.username or parsed.password or parsed.query or parsed.fragment):
            raise ValueError("Use an http(s) Dataverse URL without credentials, query or fragment")
        if parsed.scheme == "http" and parsed.hostname not in {"localhost", "127.0.0.1", "::1"}:
            raise ValueError("Use HTTPS for a remote Dataverse")
        self.url = url.rstrip("/")
        self.token = token
        self.opener = build_opener(NoRedirect())

    def request(self, path, method="GET", data=None):
        headers = {"Content-Type": "application/json"}
        if method != "GET":
            headers["X-Dataverse-key"] = self.token
        body = None if data is None else json.dumps(data).encode("utf-8")
        req = Request(self.url + "/api/licenses" + path, data=body, headers=headers, method=method)
        try:
            with self.opener.open(req, timeout=30) as response:
                result = json.load(response)
        except HTTPError as error:
            raise RuntimeError(f"{method} licenses{path}: HTTP {error.code}") from None
        except URLError:
            raise RuntimeError("Cannot connect to the Dataverse license API") from None
        if result.get("status") != "OK":
            raise RuntimeError("Dataverse license API did not return OK")
        return result["data"]


def find_cc_by(licenses):
    matches = [item for item in licenses
               if item["uri"].replace("http://", "https://", 1).rstrip("/") == CC_BY_URI]
    if len(matches) > 1:
        raise ValueError("Multiple CC BY 4.0 entries; resolve duplicates before applying")
    return matches[0] if matches else None


def apply_default(client):
    license = find_cc_by(client.request(""))
    if license is None:
        definition = json.loads((ROOT / "scripts/api/data/licenses/licenseCC-BY-4.0.json").read_text())
        client.request("", "POST", definition)
        license = find_cc_by(client.request(""))
        if license is None:
            raise RuntimeError("CC BY 4.0 was not returned after creation")
    license_id = int(license["id"])
    if not license["active"]:
        client.request(f"/{license_id}/:active/true", "PUT")
    if not license["isDefault"]:
        client.request(f"/default/{license_id}", "PUT")
    licenses = client.request("")
    defaults = [item for item in licenses if item["isDefault"]]
    if len(defaults) != 1 or defaults[0]["id"] != license_id or not defaults[0]["active"]:
        raise RuntimeError("Could not verify CC BY 4.0 as the active default")
    return licenses


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--url", default=os.environ.get("SERVER_URL", "http://localhost:8080"))
    parser.add_argument("--apply", action="store_true", help="Set the default for new datasets")
    parser.add_argument("--token-file", type=Path, help="Superuser API token file (or set API_TOKEN)")
    args = parser.parse_args()
    token = ""
    if args.apply:
        token = (args.token_file.read_text().strip() if args.token_file
                 else os.environ.get("API_TOKEN", "").strip())
        if not token:
            parser.error("--apply requires --token-file or API_TOKEN")
    client = Client(args.url, token)
    licenses = apply_default(client) if args.apply else client.request("")
    print(json.dumps({"licenses": [
        {key: item[key] for key in ("id", "name", "active", "isDefault")}
        for item in licenses]}, ensure_ascii=False, indent=2))


if __name__ == "__main__":
    try:
        main()
    except (OSError, ValueError, RuntimeError) as error:
        print(f"Error: {error}", file=sys.stderr)
        sys.exit(1)
