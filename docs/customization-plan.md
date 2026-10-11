# UaRD implementation plan

This plan replaces the earlier generic homepage sequence with the supplied UaRD
brief. It covers incremental Dataverse 6.12 implementation. The contract's separate
Figma/prototype deliverables and calendar are not represented as completed here.

## 1 Hero and search — revised implementation

- Use the full UaRD name and institutional subtitle instead of a generic slogan.
- Use the original circuit logo from page 1 of the supplied PDF.
- Compact, full-width pale-blue hero with a 1280px content limit.
- Native UTF-8 GET search, an accessible icon button, Advanced search, and Browse
  collections linking to native Dataverse routes.
- Separate English/Ukrainian UI labels and editorial JSON.
- Ukrainian first visit through JSF configuration; subsequent user choices remain
  in DataverseSession. No frontend-only forced language or second language store.
- Selected generated laptop-and-flag background integrated as a 30 KB WebP;
  non-cropping sizing, responsive overlay and CSS fallback retained. Original logo/text/search remain separate.

Acceptance: fresh session starts Ukrainian even with an English browser preference;
UA → EN → UA works; search encoding/Enter/button and advanced/browse routes work;
desktop/tablet/mobile layout, keyboard focus, 40% longer labels, no-JS form and
contrast checks pass. Final visual artwork is a replaceable asset, not a blocker
to functional hero completion.

Files: `customization/homepage/{template.html,homepage.js,locales/*,content/*,assets/*}`,
`customization/branding/{custom-homepage.html,custom-stylesheet.css}`,
`scripts/customization/build-homepage.py`,
`src/main/webapp/WEB-INF/faces-config.xml`, and documentation.

## 2 Header styling and shared visual foundations — styling pass implemented

Implemented a CSS-only 64px white/navy navbar, responsive dropdowns and controls,
and removed the empty homepage message-panel gap. See `docs/header-style-verification.md`.
Authenticated role-based QA and native Ukrainian bundle activation remain open.

Continue shared colors, typography, 8px spacing, focus, buttons and fields.
Style the existing header first; agree on UaRD / DNTB / Dataverse hierarchy.
Keep search accessible on internal pages. Confirm five navigation groups and
destinations before introducing menu items. Complete the header's Ukrainian labels
using the mounted bundles. Reuse the official logo.

Acceptance: desktop/mobile, signed out, authenticated, superuser, language controls,
search, support, account/notifications and all Add Data permission combinations.
Check logo clear space and mobile collapse. First shared-shell visual checkpoint.

## 3 Minimal header structure and navigation — after the layout is agreed

Change wrappers/classes in `dataverse_header.xhtml` only where CSS is insufficient.
Preserve forms, component IDs, actions, update targets, rendered checks and widget
behavior. Preserve the standard breadcrumb and skip-link mechanisms.
Record every upstream change and deploy with FRD.

## 4 Organizations and institutions — initial strip implemented

Prepared five proposed institution payloads and create/publish/feature commands in
`docs/example-collections-api.md`. The user created/published/featured all five collections. The homepage now renders
the native featured list as an accessible horizontal strip; see
`docs/homepage-collections.md` for behavior, verification and remaining logo/content work.

Confirm collection aliases and logo provenance; use the standard featured list.
Build an ordered scroll-snap strip with keyboard/buttons/swipe, position indication,
a visible next-card cue on small screens, and loading/empty/error states.
View all must lead to a genuine grid/directory; native filtered browse may serve as
an explicitly documented interim destination. No placeholder counts or sample
institutions should be presented as live repository entries.

## 5 Knowledge areas and clusters

Knowledge-area placeholder strip implemented with user-supplied A–K translations
and blue SVG folders. The cluster strip also has all 15 bilingual labels and
distinct single-blue SVG icons. Card destinations remain unconfigured; see
`docs/homepage-clusters.md`.
See `docs/homepage-knowledge-areas.md`.

Agree the taxonomy and backend mappings first. Implement two distinct sections
with consistent SVG icons and separate translated editorial data.
Provide valid browse/filter destinations and accessible View all grids.
Avoid Java/custom endpoints unless native APIs demonstrably cannot support the model.

## 6 Collection, search and result templates — P1

Carry the shared styles into collection search, advanced search, facets, selected
filter chips, sort, pagination and result-card variants. Preserve current JSF
commands, visibility and permissions. Assess autocomplete and quick filters against
the native search API before promising behavior. Define consistent dates/statuses,
including empty/loading/error states and breadcrumbs.

## 7 Dataset page — P1

Improve hierarchy for title/authors, identifier, citation, license/version, metadata
and file table. Preserve download restrictions, version actions and native copy,
cite/share functionality. Verify English/Ukrainian and all three responsive widths.

## 8 Notices and footer — custom footer implemented; notices pending

Custom blue bilingual footer and thin native legal strip are implemented via
branding hook and CSS; see `docs/custom-footer.md`. About, Policies, EOSC, OpenAIRE,
re3data, FAIR, Contacts and About DNTB have article destinations. Documents,
For institutions and Statistics remain placeholders. Links/pending text now share
compact muted desktop rows and uniform larger mobile/coarse-pointer rows.

Style three notice levels with appropriate contrast, icon, details and dismiss
behavior using existing server logic. Do not reproduce the screenshot's unverified
DOI outage message. Build a readable institutional footer with validated DNTB,
policy, support and standards links. Preserve Dataverse attribution and relevant
widget/footer behavior.

## 9 Remaining P2 pages and states

View all directories, account/authentication, deposit/upload, editorial templates,
service/error/maintenance states and cookie UI only where actually applicable.
Include validation, disabled/loading/error states without replacing native logic.

Informational pages now use a shared native-shell pattern: bilingual /about.xhtml,
short Ukrainian /system.xhtml linked through :NavbarAboutUrl, and seven Ukrainian
footer articles. Ukrainian-only pages render the supplied text in either UI language
until English copy is ready. See the article guides and README_HANDOFF.md for routes,
metadata/locale behavior and deployment. Policies is a planned overview with pending
publication wording; groups 7-9 have regular weight and nested hierarchical numbers.
Clean URL mappings were discussed but remain unimplemented.

## 10 Review and handoff

Verify the full search → results → dataset journey, responsive/language behavior,
keyboard access, reduced motion, focus, contrast and permissions. Record verified
checks and gaps rather than claiming blanket accessibility compliance.
Maintain asset sources, tokens, build instructions, upstream diff notes and
a concise current-state handoff. Keep each feature in a focused branch/commit;
do not sweep unrelated staged user work into a commit.

## Work and chat boundaries

Keep the hero and header discussion together while visual decisions are evolving.
Start a new chat at a documented feature boundary (institutions, knowledge taxonomy,
internal templates), with this plan, CODEX_CONTEXT.md and the current Git diff.
Do not create a new chat for every small task or lose pending user decisions.
