# DEV_SETUP.md — Dataverse 6.12 Local Development

For moving the working instance to another PC, see
[New PC setup](docs/new-pc-setup.md).

## Host
Windows 11 with WSL2 Ubuntu.

## Toolchain
Required:
- Docker Desktop
- WSL2 integration enabled for Ubuntu
- Java 21
- Maven
- Git
- VS Code with WSL support

Verify:
```bash
java -version
javac -version
mvn -version
git --version
docker --version
docker compose version
```

Docker smoke test:
```bash
docker run --rm hello-world
```

## Repository location
Keep the repository in the WSL filesystem:

```text
~/projects/dataverse
```

Do not use `/mnt/c/...` for the source tree unless there is a specific reason.

Open in VS Code:
```bash
cd ~/projects/dataverse
code .
```

VS Code should show a WSL/Ubuntu remote context.

## Base source
Official upstream:
```text
https://github.com/IQSS/dataverse.git
```

Base release:
```text
v6.12
```

## Start development environment
Preferred:
```bash
./scripts/dev/dev-start-frd.sh
```

This starts the fast-redeploy development environment.

## Redeploy after source changes
For XHTML/Java/webapp source changes:
```bash
./scripts/dev/dev-frd.sh
```

Then refresh the browser.

## Branding live-edit workflow
Branding files are bind-mounted:

Host:
```text
~/projects/dataverse/customization/branding
```

Container:
```text
/var/www/dataverse/branding
```

Changes to branding HTML/CSS should require only save + browser refresh.

## Language files
Host:
```text
~/projects/dataverse/customization/lang
```

Container:
```text
/var/www/dataverse/lang
```

Expected JVM option:
```text
-Ddataverse.lang.directory=/var/www/dataverse/lang
```

## Useful diagnostics

Check app version:
```bash
curl http://localhost:8080/api/info/version
```

Check mount:
```bash
docker inspect dev_dataverse \
  --format '{{range .Mounts}}{{println .Source "->" .Destination}}{{end}}'
```

Inspect branding:
```bash
docker exec dev_dataverse ls -la /var/www/dataverse/branding
```

Inspect language files:
```bash
docker exec dev_dataverse ls -la /var/www/dataverse/lang
```

If the page shows only "Hello from Payara" after container recreation, redeploy:
```bash
./scripts/dev/dev-frd.sh
```

## Notes
The existing `docker-compose.override.yml` is used by the FRD startup script if present. Keep local mount changes there rather than editing the upstream `docker-compose-dev.yml`.

## Custom homepage: hero and search

The homepage is a generated HTML fragment served through `:HomePageCustomizationFile`.
Its source files are:

- `customization/homepage/template.html`: semantic hero markup and native GET search form.
- `customization/homepage/homepage.js`: reads the JSF document locale and selects translations.
- `customization/homepage/locales/en.json` and `uk.json`: interface labels.
- `customization/homepage/content/home.en.json` and `home.uk.json`: editorial text.
- `customization/branding/custom-stylesheet.css`: scoped homepage styling.

After editing homepage markup, JavaScript or JSON, run from the WSL repository:

```bash
python3 scripts/customization/build-homepage.py
```

This uses only the Python standard library and writes
`customization/branding/custom-homepage.html`. Do not edit that generated file directly.
Refresh the browser to see the result. CSS edits need only a browser refresh.
No Maven build or FRD redeploy is required for these mounted branding files.

Check that the generated artifact matches its sources:

```bash
python3 scripts/customization/build-homepage.py --check
```

The generated fragment references versioned public application resources under
`/resources/images/uard/` and `/resources/js/uard/`; safe inert translation JSON
remains embedded. The branding mount is not a public asset directory. UI/editorial
translations remain separate in editable sources. The static Ukrainian fallback
and native search form work without JavaScript; native switching and client
homepage/footer translation require the normal Dataverse scripts.

Search submits `q` to `/dataverse.xhtml` with native UTF-8 GET encoding.
The browse link uses `/dataverse.xhtml?types=dataverses`.
Both destinations use the existing Dataverse browse/search UI.

### Dataverse language selector

The homepage reads `document.documentElement.lang`; it does not use browser language
preferences or maintain its own locale. Both `ua` and `uk` map to the homepage's
`uk` translations. Unknown locales fall back to English.

During step 1, the running local instance was missing `:Languages`. The setting
was restored to the following value, matching the existing `Bundle_ua.properties`
convention:

```json
[{"locale":"en","title":"English"},{"locale":"ua","title":"Українська"}]
```

For a recreated local development database, restore it using the existing local
admin API access:

```bash
curl -X PUT -H 'Content-Type: application/json' \
  --data '[{"locale":"en","title":"English"},{"locale":"ua","title":"Українська"}]' \
  http://localhost:8080/api/admin/settings/:Languages
```

The hero translation is independent of the Dataverse resource bundles. Translation
of the rest of the UI still requires the language-directory JVM option documented
above; during verification, the existing navbar labels remained English when
Ukrainian was selected.

### Step 1 verification (2026-10-03)

Verified against the running Dataverse 6.12 using an isolated headless Chrome session:

- Actual JSF language selection: English → Ukrainian → English.
- English, Ukrainian and mixed special-character queries preserve the exact `q`
  value on the existing search page and return HTTP 200.
- The browse link selects the dataverses filter.
- Desktop, 768px tablet and 375px/320px mobile layouts have no horizontal overflow.
- Keyboard focus moves from the search field to its submit button.
- Native search still submits with JavaScript disabled.
- No JavaScript page errors; desktop/mobile screenshots inspected in both languages.
- Generator consistency and Git whitespace checks pass.

No upstream XHTML/Java files were changed. The pre-existing deployed navbar test
markup remains a separate header-work concern.

## UaRD requirements revision

The revised implementation and plan are documented in:
- `docs/uard-requirements.md`: source assessment and outstanding decisions.
- `docs/customization-plan.md`: phased implementation and acceptance criteria.
- `docs/hero-image-brief.md`: ChatGPT image prompt and optional artwork integration.
- `docs/uard-hero-verification.md`: current browser/contrast checks.

The hero now uses the full UaRD title, supplied circuit logo, compact banner,
native advanced-search link and Ukrainian static fallback. The logo source and
PNG rendering are in `customization/homepage/assets/` with provenance notes.

The current background is served from the application resource
`src/main/webapp/resources/images/uard/hero-background.webp`; original/unused
artwork remains in local design archives. Resource changes require application
deployment before publishing matching versioned HTML; see docs/resource-assets.md.

**The earlier no-upstream-change statement applies only to the first generic
hero iteration.** The UaRD revision changes `WEB-INF/faces-config.xml` to default
new JSF sessions to Ukrainian and has been deployed with FRD. See
`docs/upstream-modifications.md`. Existing sessions keep an explicit English
selection; use a fresh browser context to verify first-visit behavior.

## Current informational pages and footer workflow (2026-10-11)

Read README_HANDOFF.md for the current nine-page inventory and committed state.
Wrappers live in src/main/webapp; editorial fragments live in uard/content beneath
it. /about.xhtml is bilingual; System and the seven newer footer articles currently
render Ukrainian in either UI language. All reuse the native JSF template/session.
Do not copy header/footer markup into articles or add an independent language store.

For new article routes, deploy their fragments/wrappers with ./scripts/dev/dev-frd.sh,
check the routes, then regenerate/publish custom-footer.html from links.json.
The Policies hierarchical list also requires its scoped current stylesheet rules.
CSS-only footer/header-spacing updates refresh through the existing branding mount.

The native top-menu hook is currently :NavbarAboutUrl=http://localhost:8080/system.xhtml.
This is a local database setting, not a Git artifact. Remote deployment uses a
confirmed public destination and separate admin setting through its secured procedure.
The dummy custom-header placeholder remains unrelated staged work.

Detailed guides: docs/custom-about.md, custom-system.md, ecosystem-pages.md,
operator-pages.md, policies-page.md and custom-footer.md. Clean URL aliases were
only discussed and are not configured. Native Ukrainian UI coverage remains
incomplete despite the translated custom articles/homepage/footer.
