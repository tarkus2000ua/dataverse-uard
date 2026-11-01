#!/usr/bin/env python3
"""Build complete Ukrainian guide pages with direct chapter and heading URLs."""
import argparse
import html
from html.parser import HTMLParser
from pathlib import Path
import re
from urllib.parse import urlsplit, unquote

ROOT = Path(__file__).resolve().parents[2]
SOURCE = ROOT / "customization/guide"
OUTPUT = ROOT / "src/main/webapp/guide"
CHAPTERS = (
    "welcome.html", "account.html", "find-use-data.html",
    "dataverse-management.html", "dataset-management.html",
    "tabular-data-ingest.html", "appendix.html",
)
PAGES = ("index.html",) + CHAPTERS


class GuideLinks(HTMLParser):
    def __init__(self):
        super().__init__()
        self.ids = set()
        self.links = []
        self.images = []
        self.heading = []
        self.in_heading = False

    def handle_starttag(self, tag, attributes):
        attributes = dict(attributes)
        if attributes.get("id"):
            if attributes["id"] in self.ids:
                raise ValueError("Duplicate guide ID: " + attributes["id"])
            self.ids.add(attributes["id"])
        if tag == "a":
            target = attributes.get("hx-get", attributes.get("href"))
            if target:
                self.links.append(target)
        if tag == "img" and attributes.get("src"):
            self.images.append(attributes["src"])
        if tag == "h1":
            self.in_heading = True

    def handle_endtag(self, tag):
        if tag == "h1":
            self.in_heading = False

    def handle_data(self, data):
        if self.in_heading:
            self.heading.append(data)


def public_links(content):
    """Convert supplied fragment navigation to normal shareable page links."""
    def rewrite(match):
        tag = match[0]
        request = re.search(r'\shx-get="([^"]+)"', tag)
        if request:
            destination = request[1]
            if destination == "welcome.html":
                destination = "./"
            if re.search(r'\shref="[^"]*"', tag):
                tag = re.sub(r'\shref="[^"]*"', lambda _: ' href="' + destination + '"', tag)
            else:
                tag = tag[:-1] + ' href="' + destination + '">'
        return re.sub(r'\shx-[a-z-]+="[^"]*"', "", tag)
    return re.sub(r"<a\b[^>]*>", rewrite, content)


def navigation(template, chapter):
    def mark(match):
        tag = match[0]
        target = re.search(r'\shref="([^"]+)"', tag)
        tag = re.sub(r'\s(?:class|aria-current)="[^"]*"', "", tag)
        destination = "welcome.html" if target and target[1] == "./" else target[1] if target else ""
        if destination == chapter:
            tag = tag[:-1] + ' class="active" aria-current="page">'
        return tag
    return re.sub(r"<a\b[^>]*>", mark, template)


def build():
    template = (SOURCE / "index.html").read_text(encoding="utf-8")
    if template.count("{{guideContent}}") != 1 or template.count("{{guideTitle}}") != 1:
        raise ValueError("Expected one content and one title placeholder in the guide template")
    sources = {name: (SOURCE / name).read_text(encoding="utf-8") for name in CHAPTERS}
    parsed = {}
    for name, content in sources.items():
        parser = GuideLinks()
        parser.feed(content)
        if not parser.heading:
            raise ValueError("Missing chapter heading: " + name)
        parsed[name] = parser
    for name, page in parsed.items():
        for link in page.links:
            url = urlsplit(link)
            if url.scheme or url.netloc:
                continue
            destination = url.path or name
            if destination in {".", "./", "index.html"}:
                destination = "welcome.html"
            if destination not in parsed:
                raise ValueError("Unexpected guide link: " + name + ": " + link)
            if url.fragment and unquote(url.fragment) not in parsed[destination].ids:
                raise ValueError("Missing guide section: " + name + ": " + link)
    files = {}
    for filename in PAGES:
        chapter = "welcome.html" if filename == "index.html" else filename
        title = "Посібник користувача Dataverse UA" if chapter == "welcome.html" else (
            "".join(parsed[chapter].heading).strip() + " — Посібник UaRD"
        )
        page = navigation(template, chapter)
        page = page.replace("{{guideContent}}", public_links(sources[chapter]))
        page = page.replace("{{guideTitle}}", html.escape(title))
        files[filename] = page.encode("utf-8")
        parser = GuideLinks()
        parser.feed(page)
        if parser.in_heading:
            raise ValueError("Unclosed chapter heading: " + filename)
    for name, page in parsed.items():
        for image in page.images:
            url = urlsplit(image)
            if url.scheme or url.netloc:
                continue
            relative = unquote(url.path)
            image_path = SOURCE / relative
            if (not relative.startswith("pics/") or ".." in Path(relative).parts
                    or image_path.suffix.lower() not in {".jpg", ".jpeg", ".png", ".gif", ".webp", ".svg"}
                    or not image_path.resolve().is_relative_to(SOURCE.resolve())):
                raise ValueError("Unexpected local guide image: " + name + ": " + image)
            if not image_path.is_file():
                raise ValueError("Missing local guide image: " + name + ": " + image)
            files[relative] = image_path.read_bytes()
    return files


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--check", action="store_true", help="Verify generated guide pages and pictures")
    args = parser.parse_args()
    files = build()
    if args.check:
        stale = [name for name, data in files.items()
                 if not (OUTPUT / name).is_file() or (OUTPUT / name).read_bytes() != data]
        if stale:
            parser.exit(1, "Guide output is stale: " + ", ".join(stale) + "\n")
        print("Guide pages, direct links, section anchors and local images match their sources.")
    else:
        for name, data in files.items():
            destination = OUTPUT / name
            destination.parent.mkdir(parents=True, exist_ok=True)
            destination.write_bytes(data)
        print("Prepared " + str(len(files)) + " complete guide pages/pictures for fast redeploy.")


if __name__ == "__main__":
    main()
