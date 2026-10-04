#!/usr/bin/env python3
"""Generate the supported footer fragment without third-party dependencies."""
import argparse
import html
import json
from pathlib import Path
import re
from urllib.parse import urlsplit
from resource_assets import IMAGES, script_output, versioned_url, write_script

ROOT = Path(__file__).resolve().parents[2]
SOURCE = ROOT / "customization/footer"
OUTPUT = ROOT / "customization/branding/custom-footer.html"


def build():
    translations = {lang: json.loads((SOURCE / f"{lang}.json").read_text(encoding="utf-8")) for lang in ("en", "uk")}
    if translations["en"].keys() != translations["uk"].keys():
        raise ValueError("Footer translation keys must match")
    for values in translations.values():
        if not all(isinstance(v, str) and v.strip() for v in values.values()):
            raise ValueError("Footer translations must be nonempty strings")
    links = json.loads((SOURCE / "links.json").read_text(encoding="utf-8"))
    text = (SOURCE / "template.html").read_text(encoding="utf-8")
    def item(key):
        label = translations["uk"].get(key, {"eosc": "EOSC", "openaire": "OpenAIRE", "re3data": "re3data", "fair": "FAIR"}.get(key))
        url = links[key]
        attribute = f' data-footer-text="{key}"' if key in translations["uk"] else ""
        if url is None:
            node = f'<span class="uard-footer__pending"{attribute}>{html.escape(label)}</span>'
        else:
            if not isinstance(url, str) or not (url.startswith("/") and not url.startswith("//") or urlsplit(url).scheme == "https"):
                raise ValueError(f"Invalid footer URL: {key}")
            node = f'<a href="{html.escape(url, quote=True)}"{attribute}>{html.escape(label)}</a>'
        return f"<li>{node}</li>"
    for group, keys in {"repository": ["about", "policies", "documents", "institutions"], "support": ["statistics"], "ecosystem": ["eosc", "openaire", "re3data", "fair"], "operator": ["contacts", "aboutOperator"]}.items():
        text = text.replace("{{" + group + "Items}}", "\n".join(item(key) for key in keys))
    text = re.sub(r"\{\{([a-zA-Z]+)\}\}", lambda m: html.escape(translations["uk"][m[1]]) if m[1] in translations["uk"] else m[0], text)
    logo = versioned_url(IMAGES / "uard-logo.png")
    payload = json.dumps(translations, ensure_ascii=False).replace("&", r"\u0026").replace("<", r"\u003c").replace(">", r"\u003e")
    js_path, js_data = script_output(SOURCE / "footer.js")
    return text.replace("{{logo}}", logo).replace("{{translations}}", payload).replace("{{scriptUrl}}", versioned_url(js_path, js_data))


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--check", action="store_true")
    parser.add_argument("--resources-only", action="store_true", help="Prepare JS without activating new branding HTML")
    args = parser.parse_args()
    if args.check and args.resources_only:
        parser.error("--check and --resources-only are mutually exclusive")
    result = build()
    js_path, js_data = script_output(SOURCE / "footer.js")
    if args.check:
        if not js_path.exists() or js_path.read_bytes() != js_data:
            parser.exit(1, "Footer JavaScript is stale. Rebuild the footer.\n")
        if not OUTPUT.exists() or OUTPUT.read_text(encoding="utf-8") != result:
            parser.exit(1, "Footer is stale. Run python3 scripts/customization/build-footer.py\n")
        print("Footer matches its sources.")
    else:
        write_script(js_path, js_data)
        if args.resources_only:
            print("Prepared footer JavaScript; branding HTML unchanged.")
        else:
            OUTPUT.write_text(result, encoding="utf-8")
            print("Generated customization/branding/custom-footer.html")
