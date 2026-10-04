# UaRD application resources

Runtime images and reusable SVG artwork live in
src/main/webapp/resources/images/uard/. Those files are the canonical editable
assets; no duplicate runtime copy is maintained under customization/homepage.
Original PDFs and unused generated artwork remain local design archives.

Editable JavaScript remains in customization/homepage/homepage.js and
customization/footer/footer.js. Builders write deployment copies under
src/main/webapp/resources/js/uard/. Do not edit generated JS copies.

HTML stays in the supported branding hooks. It references absolute /resources/
URLs with a SHA-256 content version query. Image dimensions/empty decorative alt
attributes are explicit. Small search/arrow/institutions UI SVGs remain inline.
Safe inert translation JSON stays in the HTML; external deferred scripts read it
and continue using the native JSF document locale. No extra translation requests.

## Build and deploy

Standard application build preparation:

```bash
python3 scripts/customization/build-homepage.py
python3 scripts/customization/build-footer.py
python3 scripts/customization/build-homepage.py --check
python3 scripts/customization/build-footer.py --check
```

Prepare resources before updating live mounted fragments (local FRD workflow):

```bash
python3 scripts/customization/build-homepage.py --resources-only
python3 scripts/customization/build-footer.py --resources-only
./scripts/dev/dev-frd.sh
python3 scripts/customization/build-homepage.py
python3 scripts/customization/build-footer.py
```

The resource-only mode updates JS deployment copies without replacing live HTML.
For production deploy the new WAR/resources before publishing the corresponding
branding fragments. Resource files are public, so never place credentials or
private content in them. JS/image changes require deployment. Branding HTML/CSS
remain mounted; CSS-only edits still refresh directly. Translation changes also
refresh directly if the script itself has not changed. Commit generated JS and
HTML together with sources and resource images.

The builders validate required asset availability, content versions and generated
script consistency. A modified generated copy causes --check to fail. Production
cache behavior depends on server configuration; version queries avoid reusing
old content for a new asset reference, without introducing hashed file cleanup.

## Migration verification

FRD succeeded; all image/script requests completed without resource errors.
Verified external deferred scripts, all SVG/PNG images decoded, WebP dimensions
2172 × 724, six live featured cards, static-strip round trips, disabled edge
buttons and single complete cards at mobile widths. Layout checks at
1440/768/375/320px showed no horizontal overflow or footer-bottom gap.
Native English selection updated homepage, taxonomy labels and footer; search
preserved mixed-language query text; support dialog opened. JavaScript-disabled
static cards/images remained available. No page errors observed.
Homepage HTML: 96,067 → 22,969 bytes. Footer HTML: 17,785 → 3,843 bytes.
Hero and mobile cluster screenshots visually inspected.
