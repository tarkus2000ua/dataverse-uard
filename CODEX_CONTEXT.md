# CODEX_CONTEXT.md - Current Project State

## Current handoff and PC transfer (2026-10-12)

Current implementation commit is dabff35642 on codex/homepage-hero, adding all
11 knowledge-area and 15 cluster links to native /dataverse/uard filters.
See docs/homepage-navigation.md for canonical values and the local/remote schema
difference. This supersedes earlier missing-link/placeholder notes below.
The user explicitly requested committing remaining useful setup/design docs and
helpers and pushing the branch to origin. Inspect current Git/remote state before
claiming a push succeeded. README_HANDOFF.md and docs/new-pc-setup.md describe the
current handoff and private transfer of runtime data; original design archives
and the staged dummy header remain outside this documentation commit.


## Latest verified state (2026-10-11)

This section and README_HANDOFF.md supersede older implementation notes below.
Current branch: codex/homepage-hero. Latest commit: 943b25dea0, preceded by
44a3945440 and 06fbd32c3f. These implementation changes are committed and locally
deployed; none were pushed or remotely deployed by the agent. The handoff/context
files were untracked at that checkpoint; the 2026-10-12 publication request
supersedes that status. Preserve the staged dummy header and any unrelated work.

- Homepage hero uses the supplied updated Ukrainian/English repository names and
  short linked Dataverse attribution. Original institution-logo loading/fallbacks
  remain intact.
- /about.xhtml is bilingual, selecting separate editorial fragments server-side
  from DataverseSession.localeCode and localizing title/description.
- /system.xhtml is the short top-menu article, Ukrainian in either UI language.
  Local :NavbarAboutUrl=http://localhost:8080/system.xhtml uses the native hook;
  configure a remote public URL separately. Native header source is unchanged.
- Seven further footer pages are Ukrainian in either interface language: EOSC,
  OpenAIRE, re3data, FAIR, Contacts, About DNTB and Policies. Their links carry
  hreflang=uk. Documents/institutions/statistics remain null destinations.
- Policies has semantic nested ordered lists and hierarchical CSS numbering;
  groups 7-9 use regular weight. Supplied pending-publication wording remains.
- Footer links/support/pending rows share compact muted desktop styling and larger
  uniform mobile/coarse-pointer targets. Hover/focus remain visible.
- Fixed-navbar padding remains 64px; only empty message-panel spacing is suppressed
  on non-widget homepage/article pages.
- Native locale is authoritative. Local en/ua behavior and Ukrainian fallback are
  retained; complete native Ukrainian translation/authenticated QA remain open.
- Clean /about/ and /system/ URLs were discussed, not implemented. PrettyFaces
  mappings can be added in a later explicitly requested change.

See docs/custom-about.md, custom-system.md, ecosystem-pages.md, operator-pages.md,
policies-page.md, custom-footer.md and the latest incremental remote-deployment.md
sections. README_HANDOFF.md contains the full routes, source layout, checks and
current deployment-artifact caveats. The user lacks full remote access; prepare
administrator-ready packages/instructions. Actual active Payara path/port and
remote deployment completion remain unconfirmed. Earlier remote About 404 was
not confirmed resolved. Preserve maintained remote localization.

## Historical notes

The following sections describe earlier iterations and possible plans; do not
interpret their older "not committed", "links missing" or React suggestions as
current implementation status.

## Public resource assets (2026-10-04)

Runtime images and SVGs now live in src/main/webapp/resources/images/uard.
JavaScript sources remain under customization/homepage and customization/footer;
builders generate deployed copies in resources/js/uard. HTML uses versioned
resource URLs with deferred scripts; safe translation JSON remains inline.
See docs/resource-assets.md for the resource-first FRD workflow and verification.
Latest mobile/footer fixes were committed as d7e42fe061 before this migration.
The resource migration has been deployed locally and is not committed yet.

### Earlier project state

## Custom footer update (2026-10-04)

Custom footer hook enabled, with bilingual dark-blue columns and the native legal
row retained below. Standard logo/version hidden through scoped CSS. No upstream
footer changes. Build with scripts/customization/build-footer.py; see
docs/custom-footer.md. Article/resource destinations remain unconfigured.

## Current UaRD revision (2026-10-03)

This section supersedes the earlier generic hero notes below.
- Read the supplied Ukrainian UI/UX requirements, homepage reference and logo PDF.
- Product assessment: `docs/uard-requirements.md`; current plan:
  `docs/customization-plan.md`; image prompt: `docs/hero-image-brief.md`.
- Step 1 now uses the full UaRD name, supplied PDF page-1 circuit logo, compact
  full-width pale-blue hero, basic search, native Advanced search and Browse collections.
- User confirmed Ukrainian for first-time visitors. Implemented via
  `src/main/webapp/WEB-INF/faces-config.xml` and deployed with FRD.
- Explicit English choices remain managed by DataverseSession; the hero normalizes
  legacy server locale `ua` to `uk`. Static fallback is now Ukrainian.
- Logo asset/source and provenance are retained under `customization/homepage/assets/`.
- Optional `hero-background.webp` input is supported by the builder (max 300 KB).
  Selected generated laptop-and-flag version is integrated as a 30,356-byte WebP,
  with non-cropping contain sizing and a stronger tablet/mobile overlay.
- English institutional wording remains provisional pending confirmation.
- Native navbar labels remain English in Ukrainian mode; full bundle setup/review
  belongs to the shared-header work.
- Browser and contrast verification: `docs/uard-hero-verification.md`.
- Current branch remains `codex/homepage-hero`; existing staged user work is preserved.
- Header CSS first pass is implemented; empty homepage message spacing is fixed.
  See `docs/header-style-verification.md` for checks and remaining role/translation QA.
- Cluster placeholder strip now displays 15 bilingual labels with unique blue SVG
  icons; see `docs/homepage-clusters.md`. Card links remain unconfigured.
- Knowledge-area placeholder strip now displays A–K in both languages; no links
  activated yet. See `docs/homepage-knowledge-areas.md`.
- Homepage featured collection strip is implemented against the live native APIs.
  The user created and featured five institutions; see `docs/homepage-collections.md`.
  Logos and bilingual stored-name mappings remain follow-ups.
- Next: finish shared foundations/minimal header structure, then
  knowledge areas/clusters, internal P1 pages, notices/footer and remaining P2 work.
- See `docs/upstream-modifications.md` for the locale-config diff and the historical
  deployed header experiment, which was reset to current source by FRD.


## Latest implementation — Step 1 (2026-10-03)

- Feature branch: `codex/homepage-hero`, created from `custom/6.12`.
- Implemented a responsive bilingual hero, search form and collections browse link.
- Homepage uses small HTML/JavaScript sources in `customization/homepage/` with
  separate `locales/` UI strings and `content/` editorial text.
- Build with `python3 scripts/customization/build-homepage.py`; output is the
  existing mounted `customization/branding/custom-homepage.html`.
- Styles are scoped to `.dv-home` in the mounted custom stylesheet.
- Reads Dataverse's document locale, normalizing `ua`/`uk` to homepage `uk`.
- Restored missing local `:Languages` setting to English (`en`) and Ukrainian (`ua`).
- Tested actual language switching, search encoding, collections navigation,
  keyboard focus, no-JavaScript search and responsive layouts.
- Existing staged customization/setup work was preserved. No upstream source edits.
- No FRD redeploy was needed; the mounted branding updates on browser refresh.
- The existing navbar still uses English UI labels in Ukrainian mode; full
  Dataverse bundle activation/review remains separate from hero translation.
- Next planned step: style the existing header to match the hero (CSS first).
  Collections carousel and knowledge-area sections are not implemented yet.
- See `DEV_SETUP.md` for source paths, build instructions and verification details.

The older architecture notes below describe possible future work; React is not
used by the current hero implementation.


## What has already been completed

### Local development environment
Working successfully:
- Windows 11
- WSL2 Ubuntu
- Docker Desktop with WSL integration
- Java 21
- Maven
- Git
- Dataverse 6.12 source cloned to:
  ```text
  ~/projects/dataverse
  ```

The repository was checked out from Dataverse `v6.12`.

A local branch was created:
```text
local-customization-6.12
```

The intended future Git model is:
```text
upstream -> official IQSS/dataverse
origin   -> organization/private fork
```

### Dataverse runtime
The official Docker-based development environment works.

Dataverse is available at:
```text
http://localhost:8080
```

Admin login works.

The fast redeploy workflow is confirmed working:

```bash
./scripts/dev/dev-start-frd.sh
./scripts/dev/dev-frd.sh
```

A test source change to `dataverse_header.xhtml` was successfully redeployed and visible in browser DevTools.

### Branding
A local branding directory exists:

```text
customization/branding/
```

It contains at least:
```text
custom-homepage.html
custom-header.html
custom-footer.html
custom-stylesheet.css
```

It is bind-mounted into the Dataverse container at:

```text
/var/www/dataverse/branding
```

The mount is configured in:

```text
docker-compose.override.yml
```

Expected override fragment:

```yaml
services:
  dev_dataverse:
    volumes:
      - ./customization/branding:/var/www/dataverse/branding
      - ./customization/lang:/var/www/dataverse/lang
```

The FRD startup script detects `docker-compose.override.yml`, so the branding mount works together with the FRD workflow.

### Supported customization behavior confirmed
The following work:
- `HomePageCustomizationFile`
- `FooterCustomizationFile`
- `StyleCustomizationFile`
- `HeaderCustomizationFile`

Important discovery:
- the custom header is additional content and does **not** replace the standard Dataverse navbar;
- because the standard navbar uses `navbar-fixed-top`, the custom header can appear visually obscured/underneath it;
- the actual navbar structure is in:
  ```text
  src/main/webapp/dataverse_header.xhtml
  ```

### Header source modification
A harmless source change was tested:
```xhtml
<nav id="dataverse-header-block" data-local-dev="true">
```

After:
```bash
./scripts/dev/dev-frd.sh
```

the change appeared in DevTools, proving the XHTML source workflow works.

A temporary brand text change was also tested successfully.

## Ukrainian support
Ukrainian language support was enabled and works.

A custom language directory exists:

```text
customization/lang/
```

It includes at least:
```text
Bundle.properties
Bundle_ua.properties
```

The Ukrainian file came from the Global Dataverse Community language packs and is old, so it should be treated as a starting point rather than a complete 6.12 translation.

Dataverse language configuration uses:
```text
English
Українська
```

The configured JVM option is:

```text
-Ddataverse.lang.directory=/var/www/dataverse/lang
```

A React/custom HTML proof-of-concept successfully switched content based on the active Dataverse language.

## Current architecture direction

### Header/footer
Likely source-level XHTML modifications:
```text
src/main/webapp/dataverse_header.xhtml
```

and possibly the Dataverse footer XHTML file.

The objective is to restructure presentation while preserving:
- login/logout behavior
- user menu
- permissions
- Add Data visibility rules
- search behavior
- support behavior
- notification logic
- locale/language selector logic

### Homepage
Do **not** assume the final homepage should be XHTML/JSF.

The likely direction is:
```text
Dataverse 6.12 backend + existing JSF shell
        |
        +-- modified JSF header/footer
        |
        +-- custom React homepage
```

A complex homepage may include:
- hero section
- search
- featured collections
- latest datasets
- metrics/statistics
- cards
- editorial blocks/articles
- multilingual content
- Dataverse API integration

### Localization model
Keep these separate:

UI translation:
```text
src/locales/
  en.json
  uk.json
```

Editorial/content translation:
```text
content/
  home.en.json
  home.uk.json
```

or a CMS later if non-developers need to maintain content frequently.

The Dataverse locale should remain the source of truth for language switching.

## Recommended next task
Before implementing the real header redesign:
1. finalize target header layout;
2. map current Dataverse navbar elements to the desired structure;
3. preserve all dynamic JSF expressions/actions;
4. make incremental changes in `dataverse_header.xhtml`;
5. test logged-out, logged-in, admin, mobile, language switching, search, and Add Data behavior.
