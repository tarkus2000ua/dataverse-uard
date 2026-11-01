#!/usr/bin/env python3
"""Merge reviewed Ukrainian terminology into an existing UTF-8 Dataverse bundle."""
import argparse
import json
from pathlib import Path
import re
import sys


def build_update(raw, entries):
    """Return updated bytes and change counts; reject conflicts before any write."""
    bom = raw.startswith(b"\xef\xbb\xbf")
    text = raw.decode("utf-8-sig")
    lines = text.splitlines(keepends=True)
    wanted = {}
    for entry in entries:
        key = entry["key"]
        if key in wanted or not re.fullmatch(r"[A-Za-z0-9_.]+", key):
            raise ValueError("Invalid or duplicate update key: " + key)
        if any(ch in entry["after"] for ch in "\r\n"):
            raise ValueError("Multiline update value: " + key)
        wanted[key] = entry
    positions = {key: [] for key in wanted}
    continued = False
    for index, line in enumerate(lines):
        value = line.rstrip("\r\n")
        is_continuation = continued
        continued = (len(value) - len(value.rstrip("\\"))) % 2 == 1
        if is_continuation or value.lstrip().startswith(("#", "!")):
            continue
        match = re.match(r"^([ \t]*)([A-Za-z0-9_.]+)([ \t]*[=:][ \t]*)(.*)$", value)
        if match and match[2] in positions:
            positions[match[2]].append((index, match, continued))
    errors = []
    pending = []
    already = 0
    for key, entry in wanted.items():
        found = positions[key]
        if len(found) != 1:
            errors.append(key + ": missing or duplicate entry")
            continue
        index, match, continuation = found[0]
        if continuation:
            errors.append(key + ": continued values require manual review")
        elif match[4] == entry["after"]:
            already += 1
        elif match[4] != entry["before"]:
            errors.append(key + ": existing translation differs; review it before merging")
        else:
            ending = "\r\n" if lines[index].endswith("\r\n") else "\n" if lines[index].endswith("\n") else ""
            pending.append((index, match[1] + key + match[3] + entry["after"] + ending))
    if errors:
        raise ValueError("\n".join(errors))
    for index, value in pending:
        lines[index] = value
    result = "".join(lines).encode("utf-8")
    if bom:
        result = b"\xef\xbb\xbf" + result
    return result, len(pending), already


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("bundle", type=Path, help="Downloaded remote Bundle_uk.properties or Bundle_ua.properties")
    parser.add_argument("--patch", type=Path, default=Path(__file__).with_name("uk-collection-terminology.json"))
    mode = parser.add_mutually_exclusive_group(required=True)
    mode.add_argument("--check", action="store_true", help="Report conflicts without writing files")
    mode.add_argument("--output", type=Path, help="Write a new bundle; input and existing output files are never overwritten")
    args = parser.parse_args()
    try:
        manifest = json.loads(args.patch.read_text(encoding="utf-8"))
        if manifest.get("format") != 1 or not isinstance(manifest.get("entries"), list):
            raise ValueError("Unsupported translation update format")
        if args.output and args.output.resolve() == args.bundle.resolve():
            raise ValueError("Choose a separate output file; the input bundle is preserved")
        result, changed, already = build_update(args.bundle.read_bytes(), manifest["entries"])
        if args.output:
            with args.output.open("xb") as stream:
                stream.write(result)
            print("Created " + str(args.output))
        print(str(changed) + " entries to update; " + str(already) + " already updated. No conflicts.")
        return 0
    except (OSError, UnicodeError, ValueError, KeyError, TypeError) as error:
        print("Update stopped; input bundle unchanged.\n" + str(error), file=sys.stderr)
        return 2


if __name__ == "__main__":
    raise SystemExit(main())
