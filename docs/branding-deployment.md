# UaRD branding deployment (Dataverse 6.12)

## Build and files

Run from the repository root with Python 3 (standard library only):

```bash
python3 scripts/customization/build-homepage.py
python3 scripts/customization/build-homepage.py --check
python3 scripts/customization/build-footer.py
python3 scripts/customization/build-footer.py --check
```

Commit the generated customization/branding/custom-homepage.html alongside its
sources. Serve custom-homepage.html and custom-stylesheet.css via the supported
branding hooks. Mount customization/branding at /var/www/dataverse/branding in
the application container, or copy those files to an equivalent readable path.
The included docker-compose.override.yml configures development mounts and memory;
adapt resource limits/mounts in your production deployment rather than treating
that override as a complete production configuration.

Configure these settings through your secured local administration API:

```bash
curl --fail-with-body -X PUT --data-binary '/var/www/dataverse/branding/custom-homepage.html' http://localhost:8080/api/admin/settings/:HomePageCustomizationFile
curl --fail-with-body -X PUT --data-binary '/var/www/dataverse/branding/custom-stylesheet.css' http://localhost:8080/api/admin/settings/:StyleCustomizationFile
curl --fail-with-body -X PUT -H 'Content-Type: application/json' --data-binary '[{"locale":"en","title":"English"},{"locale":"ua","title":"Українська"}]' http://localhost:8080/api/admin/settings/:Languages
```

These commands assume local admin API access is permitted by deployment policy.
The custom footer is now provided. Build it with
`python3 scripts/customization/build-footer.py`, then configure
`:FooterCustomizationFile` as `/var/www/dataverse/branding/custom-footer.html`.
See custom-footer.md for behavior and placeholder link configuration. No custom
header hook is required; do not enable the local custom-header placeholder.
Branding HTML/CSS remain mounted. Runtime images and generated JavaScript now
load from the application resources and require deployment when changed; see
resource-assets.md for the two-stage FRD sequence.

## Locale and upstream configuration

The source change in WEB-INF/faces-config.xml makes new sessions default to legacy
locale ua. Deploy the customized application to apply it; in the existing local
FRD environment use ./scripts/dev/dev-frd.sh. Explicit language selection remains
native JSF behavior. See upstream-modifications.md for the exact change.

Homepage UI/editorial translations are separate JSON files. Native application
bundles live in customization/lang, mounted at /var/www/dataverse/lang. Activation
requires the dataverse.lang.directory JVM property pointing to that path and an
appropriate application restart/redeployment. The bundled Ukrainian translation
is older and incomplete; local native navbar labels have remained English.
Review bundle activation and coverage before production release. The custom
homepage already switches English/Ukrainian independently using the JSF locale.

## Data and unfinished destinations

Institution cards read the native root featured list anonymously. Create/publish
real child collections and configure the featured list on the target instance;
Git does not migrate collections, database settings, accounts or uploaded logos.
Development example institutions and test-university are not seeded by this commit.
Knowledge-area and cluster cards are non-navigating placeholders. Their View all
links currently use native collections browse. Configure verified taxonomy links
before treating them as finished production navigation.

## Runtime asset provenance

Runtime files are under `src/main/webapp/resources/images/uard/`.
`uard-logo.png` is the complete first-page rendering of the user-supplied institutional
logo PDF (400 × 400); the original PDF is archived locally, not required at runtime.
hero-background.webp is the selected generated laptop-and-flag image, 2172 × 724,
30,356 bytes. SVG heading, folder and cluster icons are original editable geometry.
Unused image alternatives and original image/PDF design files are excluded.

## Validation

Generator consistency, JavaScript syntax and isolated selected-file build checks
were run for this checkpoint. Whitespace checks pass for the implementation; the
pre-existing language bundles retain their original trailing whitespace warnings. Previous live checks cover responsive
scrolling and English/Ukrainian switching. Authenticated role-based header QA,
complete native Ukrainian translation and future footer/articles remain open.
