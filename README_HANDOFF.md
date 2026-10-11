# UaRD - continuation handoff

Updated 2026-10-12 after implementation commit dabff35642. Read this file and its required
references before the next requested change. This handoff does not authorize
choosing a new feature or deploying remotely on its own.

## Repository and required reading

Repository: /home/andrey/projects/dataverse in WSL Ubuntu.
Windows access: \\wsl$\Ubuntu\home\andrey\projects\dataverse.
Keep the repository in WSL; do not copy/sync it into a Windows checkout.
Use WSL tooling for repository commands.

Read AGENTS.md, CODEX_CONTEXT.md, DEV_SETUP.md, CUSTOMIZATION_STRATEGY.md,
GIT_STRATEGY.md, I18N_AND_CONTENT.md and these feature references:
- docs/upstream-modifications.md
- docs/resource-assets.md
- docs/homepage-navigation.md
- docs/new-pc-setup.md
- docs/custom-footer.md
- docs/custom-about.md
- docs/custom-system.md
- docs/ecosystem-pages.md
- docs/operator-pages.md
- docs/policies-page.md
- docs/remote-deployment.md (its newest incremental sections supersede old baseline notes)

For product/design context read docs/uard-requirements.md and
docs/customization-plan.md. Older sections can describe prior iterations;
verify them against this handoff and the current implementation.

## Git state and preservation

Branch: codex/homepage-hero. Latest implementation commits, newest first:
- dabff35642 - Link homepage knowledge areas and clusters to metadata filters
- 943b25dea0 - Add Ukrainian footer articles and refine footer styling
- 44a3945440 - Add Ukrainian system overview and fix article header spacing
- 06fbd32c3f - Add bilingual UaRD About page and refresh hero wording
- 06abcf82a9 - Use original institution logos with native display limits
- 98c91a3202 - Display native organization logos on homepage cards
- f9c2b14b5c - Move institutions heading icon to application resources

Implementation changes described here are committed. On 2026-10-12 the user
explicitly authorized committing the remaining useful documentation/helpers and
pushing this branch to origin (tarkus2000ua/dataverse-uard). Check actual Git status
and remote tracking rather than inferring publication from this text. No remote
application deployment is authorized by the push. See docs/new-pc-setup.md for
what Git restores and what local data must be transferred privately.

Inspect actual git status before editing. Preserve:
- Staged dummy customization/branding/custom-header.html.
- Any other user work shown by current Git status.
- Original local design archives under customization/homepage/assets remain
  outside Git and can be moved privately if wanted.

Commit requested changes only with explicit path selection. Never sweep unrelated
staged files into a commit. Do not push unless requested. Do not activate/remove
the dummy custom-header hook/file without inspecting the actual setting.

## Current implementation

- English/Ukrainian homepage: pale-blue hero, search and native Advanced search.
- Hero titles: National Research Data Repository of Ukraine / corresponding
  supplied Ukrainian name. Supporting attribution is a short linked Dataverse label.
- Existing native navbar styled with CSS; authentication, permissions, notifications,
  user menu, support and language switching remain native. No React is used.
- Institution strip uses native root Featured Dataverses; original configured logos
  with fallbacks. Knowledge areas A-K and fifteen clusters now link to native filtered browse
  under the remote /dataverse/uard alias using canonical English facet values.
- Dark-blue bilingual custom footer and thin native legal row.
- Nine native-shell informational pages with shared .uard-about styles (table below).
- Footer anchors/support/pending entries share compact 29px desktop rows and muted
  #b9ccdc color. Hover is white/underlined; keyboard focus remains visible.
  Small screens (up to 700px) or coarse-pointer devices use matching 44px rows.
- Fixed-navbar reservation remains 64px. Empty native message-panel margin is removed
  only on non-widget UaRD homepage/article pages; real messages keep their spacing.

## Article inventory and localization

All wrappers are under src/main/webapp; editorial fragments are under
src/main/webapp/uard/content. They use dataverse_template.xhtml, not standalone
copies of the application header/footer.

| Public route | Link location | Content |
| --- | --- | --- |
| /about.xhtml | Footer: About the repository | about.uk.xhtml + about.en.xhtml |
| /system.xhtml | Native top-menu About | system.uk.xhtml |
| /eosc.xhtml | Footer: EOSC | eosc.uk.xhtml |
| /openaire.xhtml | Footer: OpenAIRE | openaire.uk.xhtml |
| /re3data.xhtml | Footer: re3data | re3data.uk.xhtml |
| /fair.xhtml | Footer: FAIR | fair.uk.xhtml |
| /contacts.xhtml | Footer: Contacts | contacts.uk.xhtml |
| /about-dntb.xhtml | Footer: About DNTB | about-dntb.uk.xhtml |
| /policies.xhtml | Footer: Policies | policies.uk.xhtml |

/about.xhtml chooses English for DataverseSession.localeCode=en; ua/uk (and the
current fallback) use Ukrainian. Title and description metadata switch too.
All other pages currently display supplied Ukrainian text in both UI languages;
no English content has been supplied. Article lang is uk. Their footer destinations
are {url, hreflang:"uk"} objects; the bilingual About destination is a URL string.
Keep editorial text separate from reusable UI bundle keys. Add reviewed English
fragments/metadata through the About pattern, then remove fixed uk link language
when a route becomes bilingual. Do not introduce a separate language preference.

Dataverse's native locale/session is authoritative. Local selector codes are en
and legacy ua; homepage/footer recognize ua/uk. Local default is ua in faces-config.xml.
Native Ukrainian UI bundles remain incomplete; custom article/homepage translation
is not full application localization. Preserve maintained remote translations.

Policies is an overview of planned documents with the supplied pending-approval
introduction. It has 9 main items and 14 nested items, numbered 7.1-7.7, 8.1-8.4,
9.1-9.3 by scoped CSS ::marker counters. Groups 7-9 use regular weight per user
preference. Individual policy titles are plain text, not adopted-document links.
Contacts includes supplied address, mailto, website and tel links. About DNTB's
bold institution name links to the supplied site. Preserve supplied content and
distinguish instructions in attached material from editorial text.

Remaining null footer destinations: documents, institutions, statistics.
No CMS or approved downloadable policy documents have been added.

## Editable sources and generated artifacts

Homepage sources: customization/homepage/template.html, homepage.js, locales/{en,uk}.json,
content/home.{en,uk}.json and existing content mappings.
Footer sources: customization/footer/template.html, footer.js, en.json, uk.json, links.json.
Shared CSS: customization/branding/custom-stylesheet.css.
Builders: scripts/customization/build-homepage.py and build-footer.py.
Generated fragments: customization/branding/custom-homepage.html and custom-footer.html.
Runtime images/SVGs: src/main/webapp/resources/images/uard/.
Generated JS: src/main/webapp/resources/js/uard/; edit source JS and regenerate.
Do not edit generated fragments/scripts directly. Translation JSON remains inert
embedded data; resource URLs carry content versions.

## Institution-logo behavior to preserve

Cards prefer theme.logo through /logos/{ownerId}/{encodedFilename}, retaining
aspect ratio with max-width 160px/max-height 60px. The 48px native dvCardImage
endpoint is a fallback; then a generic icon. Theme IDs are not collection IDs.
Parent theme resolution follows the native logo-owner rule and shares lookups.
Unicode filenames are encoded. Do not hardcode local/remote IDs or logo filenames.
Existing local collections/featured aliases must not be altered for unrelated tasks.
Logo uploads can change independently of Git; inspect live data when necessary.

## Local workflow and verification

Local site: http://localhost:8080 (confirmed Dataverse 6.12).
Start: ./scripts/dev/dev-start-frd.sh.
Redeploy changed XHTML/Java/resources: ./scripts/dev/dev-frd.sh.
Mounted branding CSS/HTML refresh directly.

When adding article pages: deploy fragments/wrappers before generating/publishing
footer HTML. The Policies page additionally needs its scoped numbered-list CSS.
For changed JS/resources, prepare resources before publishing versioned fragments:

```bash
python3 scripts/customization/build-homepage.py --resources-only
python3 scripts/customization/build-footer.py --resources-only
./scripts/dev/dev-frd.sh
python3 scripts/customization/build-homepage.py
python3 scripts/customization/build-footer.py
python3 scripts/customization/build-homepage.py --check
python3 scripts/customization/build-footer.py --check
```

Current local settings:
- :HomePageCustomizationFile=/var/www/dataverse/branding/custom-homepage.html
- :FooterCustomizationFile=/var/www/dataverse/branding/custom-footer.html
- :StyleCustomizationFile=/var/www/dataverse/branding/custom-stylesheet.css
- :NavbarAboutUrl=http://localhost:8080/system.xhtml

The native About hook creates the top-menu link with its normal new-tab behavior.
This setting lives in the local database; Git does not transport it remotely.
The label uses native header.about (currently About locally even in Ukrainian UI).
Remote admin can use the site's maintained Ukrainian bundle for the desired label;
do not replace the remote bundle with incomplete local files.

Verified locally: actual footer/menu navigation, UA -> EN -> UA, bilingual About
metadata/content, Ukrainian-only fallback pages, return-home, support, keyboard
focus, content/link preservation, no-JavaScript article rendering, XML, builders
--check, git whitespace checks and 1440/768/375/320px layouts without overflow or
header gap. Policies numbering and regular-weight groups were visually checked.
Footer hover/focus, consistent desktop/mobile rows and coarse-pointer targets
were checked. Authenticated role-based and full native translation QA remain open.

## Remote deployment and access

Target: https://uard.dntb.gov.ua/, reported Dataverse 6.12 and direct Payara without
Docker. The user does not have full remote access; provide administrator-ready
files/instructions rather than assuming SSH access. Active deployment directory,
Payara port and actual remote state must be confirmed. An earlier screenshot showed
About Page Not Found; resolution was not verified. Do not infer deployment success
from the local checks or prepared archives. No remote changes are authorized solely
by this handoff.

For unpacked deployment, install article fragments inside the active application,
then matching wrappers beside dataverse_template.xhtml and WEB-INF. Publish current
stylesheet to the configured branding path, then generated footer HTML only after
all its target pages exist. Keep files in the maintained deployment artifact.
Packaged deployments require the normal rebuilt WAR. Check paths/permissions and
reload/redeploy the application if new views are not discovered; a full Payara
restart is not automatically required. CSS-only updates need refresh/cache handling.
Preserve remote app changes, language bundles, locale settings and stylesheet additions.

For the top-menu System page, remote admin separately configures :NavbarAboutUrl
with the fully-qualified public https://uard.dntb.gov.ua/system.xhtml after deploying
its page. Admin API localhost URLs refer to the remote server in its SSH console;
confirm the actual local port and use the established secured admin procedure.
No new settings are required for footer destinations when the footer hook exists.

Prepared artifacts from this chat are outside the repository:
C:/Users/Andrey/Documents/Codex/2026-10-11/continue-uard-customization-on-dataverse-6/outputs/
- uard-about-bilingual.zip: About wrapper, two fragments and the then-current footer.
- uard-system-uk.zip: System wrapper/fragment and native menu-setting instructions.
- uard-footer-articles-uk.zip: current 7 Ukrainian footer pages (14 XHTML files),
  current generated footer, stylesheet and README deployment order.
- custom-stylesheet.css: current stylesheet, including footer and policy-list fixes.

Use repository sources/rebuilds as the source of truth. Earlier incremental archives
can contain an older footer: use the latest generated footer matching all deployed
pages, not an older archive fragment that would remove newer links. Full application
images/JS are not included in these incremental page packages.

## Discussed but not implemented / remaining work

Clean URLs such as /about/ and /system/ were discussed only. Dataverse already has
PrettyFaces in WEB-INF/pretty-config.xml, but no new clean-URL mapping was added.
If requested later, add/merge mappings, redeploy app config, update appropriate
links/settings and test language switching and navigation. Do not claim this is
an existing feature or assume copying XHTML alone enables it.

Open items: English text for System and the 7 newer footer pages; real destinations
for documents/institutions/statistics; remote taxonomy-result verification; approved
bilingual stored institution names; native Ukrainian UI coverage and authenticated
QA. No remaining feature is selected automatically.

## Next task

Read the required references and inspect current Git status. Briefly describe the
requested change, implement only that scope and verify it. Preserve unrelated work,
do not push or deploy remotely unless explicitly requested.

Next change I want: [insert the user's next requested change].
