#!/usr/bin/env python3
"""Assemble the mounted homepage fragment using only the Python standard library."""
import argparse
import base64
import html
import json
from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[2]
SOURCE = ROOT / "customization/homepage"
OUTPUT = ROOT / "customization/branding/custom-homepage.html"


def build():
    translations = {}
    for locale in ("en", "uk"):
        translations[locale] = {
            "ui": json.loads((SOURCE / f"locales/{locale}.json").read_text(encoding="utf-8")),
            "content": json.loads((SOURCE / f"content/home.{locale}.json").read_text(encoding="utf-8")),
        }
    for group in ("ui", "content"):
        if translations["en"][group].keys() != translations["uk"][group].keys():
            raise ValueError(f"English and Ukrainian {group} keys must match")
        for locale in translations:
            if not all(isinstance(value, str) and value.strip()
                       for value in translations[locale][group].values()):
                raise ValueError(f"Empty or non-string value in {locale}/{group}")

    template = (SOURCE / "template.html").read_text(encoding="utf-8")
    areas = {locale: json.loads((SOURCE / f"content/knowledge-areas.{locale}.json").read_text(encoding="utf-8")) for locale in ("en", "uk")}
    for locale, labels in areas.items():
        if list(labels) != list("ABCDEFGHIJK") or not all(isinstance(v, str) and v.strip() for v in labels.values()):
            raise ValueError(f"Knowledge-area labels must contain A-K in order: {locale}")
        translations[locale]["knowledgeAreas"] = labels
    cards = []
    for letter, label in areas["uk"].items():
        icon = (SOURCE / f"assets/icons/knowledge-folders/folder-{letter.lower()}.svg").read_text(encoding="utf-8")
        icon = icon.replace('<svg ', '<svg aria-hidden="true" focusable="false" ', 1)
        cards.append(f'<li><div class="dv-home__knowledge-card">{icon}<span data-knowledge-area="{letter}">{letter} {html.escape(label)}</span></div></li>')
    book = (SOURCE / "assets/icons/knowledge-areas.svg").read_text(encoding="utf-8")
    template = template.replace("{{knowledgeCards}}", "\n".join(cards)).replace("{{knowledgeHeadingIcon}}", book)
    clusters = {locale: json.loads((SOURCE / f"content/clusters.{locale}.json").read_text(encoding="utf-8")) for locale in ("en", "uk")}
    if list(clusters["en"]) != list(clusters["uk"]) or len(clusters["uk"]) != 15:
        raise ValueError("Expected 15 matching ordered cluster keys")
    for locale, labels in clusters.items():
        if not all(isinstance(v, str) and v.strip() for v in labels.values()):
            raise ValueError(f"Empty cluster label in {locale}")
        translations[locale]["clusters"] = labels
    cluster_cards = []
    for key, label in clusters["uk"].items():
        if not re.fullmatch(r"[a-z-]+", key):
            raise ValueError("Invalid cluster key")
        icon = (SOURCE / f"assets/icons/clusters/{key}.svg").read_text(encoding="utf-8")
        icon = icon.replace('<svg ', '<svg aria-hidden="true" focusable="false" ', 1)
        cluster_cards.append(f'<li><div class="dv-home__knowledge-card">{icon}<span data-cluster="{key}">{html.escape(label)}</span></div></li>')
    heading_icon = (SOURCE / "assets/icons/clusters.svg").read_text(encoding="utf-8")
    template = template.replace("{{clusterCards}}", "\n".join(cluster_cards)).replace("{{clusterHeadingIcon}}", heading_icon)
    # Escape text/attributes in the Ukrainian fallback, which also works without JavaScript.
    template = re.sub(
        r"\{\{(ui|content)\.([A-Za-z]+)\}\}",
        lambda match: html.escape(translations["uk"][match[1]][match[2]], quote=True),
        template,
    )
    # Prevent editorial text from terminating the inert JSON script element.
    payload = json.dumps(translations, ensure_ascii=False, separators=(",", ":"))
    payload = payload.replace("&", "\\u0026").replace("<", "\\u003c").replace(">", "\\u003e")
    script = (SOURCE / "homepage.js").read_text(encoding="utf-8").strip()
    if "</script" in script.lower():
        raise ValueError("Inline JavaScript must not contain a closing script tag")
    logo = SOURCE / "assets/uard-logo.png"
    logo_uri = "data:image/png;base64," + base64.b64encode(logo.read_bytes()).decode("ascii")
    background = SOURCE / "assets/hero-background.webp"
    background_css = "none"
    if background.exists():
        data = background.read_bytes()
        if len(data) > 300_000:
            raise ValueError("Hero background must be at most 300 KB")
        if data[:4] != b"RIFF" or data[8:12] != b"WEBP":
            raise ValueError("Hero background must be a WebP image")
        background_css = "url(data:image/webp;base64," + base64.b64encode(data).decode("ascii") + ")"
    return (template.replace("{{translations}}", payload).replace("{{script}}", script)
            .replace("{{logo}}", logo_uri).replace("{{heroBackground}}", background_css))


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--check", action="store_true", help="Check that the generated fragment is current")
    args = parser.parse_args()
    result = build()
    if args.check:
        if not OUTPUT.exists() or OUTPUT.read_text(encoding="utf-8") != result:
            parser.exit(1, "Homepage is stale. Run python3 scripts/customization/build-homepage.py\n")
        print("Homepage matches its sources.")
    else:
        OUTPUT.write_text(result, encoding="utf-8")
        print(f"Generated {OUTPUT.relative_to(ROOT)}. Refresh Dataverse to view it.")


if __name__ == "__main__":
    main()
