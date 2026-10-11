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
attributes are explicit. All three heading icons (institutions.svg,
knowledge-areas.svg and clusters.svg) are external assets under
resources/images/uard/icons/. Small search/arrow UI SVGs remain inline.
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

## Institution card logos

Homepage organization cards use `theme.logo` from `/api/dataverses/{alias}`
to load the original `/logos/{ownerId}/{encodedFilename}` image, matching the
quality of the native featured strip rather than enlarging its 48px API thumbnail.
A shared parent lookup compares effective theme IDs to resolve inherited logos
using the same parent-directory rule as native `getLogoOwnerId()`. Theme IDs are
not collection IDs. Unicode filenames are URL-encoded.

Logos retain their natural proportions and are limited to 160px wide and 60px
high, further constrained by the card width. The adjacent organization name
provides the accessible link label. If the original cannot be resolved or loaded,
the card tries `/api/access/dvCardImage/{id}`; if that also fails or returns no
image, the generic icon remains. No separate logo copies or mappings are needed.

This changes the homepage JavaScript resource and its versioned HTML reference,
so use the resource-first FRD sequence above. There are no native template,
Java, settings or translation changes.

Verified locally on 2026-10-11 after FRD: DNTB loads `/logos/2/Logo60.png`
with a natural width of 204px and displays at approximately 160 by 47px,
instead of using the 48px API thumbnail. Browser checks covered 1440/768/375/320px
layouts without page overflow, mobile scroll round trips, original-image failure
falling back to the thumbnail, both images failing back to the generic icon,
and mocked inherited themes with Cyrillic filenames. No JavaScript page errors;
both builders' `--check` options pass. Desktop screenshot visually inspected.
