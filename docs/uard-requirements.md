# UaRD requirements and design assessment

## Sources and scope

Reviewed on 2026-10-03:
- User-supplied `В. 1 ТЗ для UaRD.docx`, including the appendix screenshot.
- User-supplied `В. 1.png`, showing the homepage structure.
- User-supplied `logo engl UARD-1.pdf`, containing six vector logo variants.

The Word document is a UI/UX design brief. Section 1.6 explicitly excludes coding
from that contract. Its Figma files, UI kit, prototype, handoff deliverables,
acceptance process and estimated 32 working days are design-contract requirements,
not evidence of completed work or a schedule for this repository. The user's
request separately authorizes updating implementation documentation and the hero.

The screenshot is a structural reference, not an instruction to reproduce every
detail: sections 3.2 and 5 specifically require improving its branding, consistency,
notifications, navigation and accessibility. The sample result, counts, dates,
DOI notice and partner labels are not verified production facts.

## Decisions

- Target Dataverse 6.12 and retain JSF authentication, permissions, session locale,
  search, contact dialogs, notifications, breadcrumbs and collection behavior.
- UaRD is the primary institutional brand; DNTB is the operating institution;
  Dataverse receives supporting platform attribution.
- User confirmed Ukrainian for first-time visitors. English remains selectable.
- Ukrainian name: «Український національний репозитарій дослідницьких даних».
- English name currently used: “Ukrainian National Research Data Repository”;
  confirmation of the official English wording remains pending.
- Use PDF page 1 (the circuit symbol above UaRD), matching the screenshot.
  Preserve source colors, proportions and clear space. Do not redraw the logo.
- Keep the compact pale-blue hero and search emphasis. Add a real Advanced search
  link. Use the official logo separately from the decorative background.
- Source files remain plain HTML/JavaScript, JSON and CSS; no React dependency is
  justified by the current hero. Later complexity may change that decision.

## Requirements mapping

| Brief | Requirement | Implementation or next action |
|---|---|---|
| 1.5, 6 | Ukrainian primary; English; room for 30–40% text growth | Hero bilingual; JSF initial locale Ukrainian; English-name approval pending |
| 3.1, 5 | UaRD logo, full name, subtitle, compact hero, search | Revised step 1 |
| 6 | Advanced search, autocomplete states and quick filters | Advanced search implemented; autocomplete/filters require a separate API/UX assessment |
| 3.2, 6 | Five menu groups, mobile navigation, breadcrumbs and skip link | Header phase; retain existing JSF controls and native breadcrumbs |
| 3.1, 6 | Organizations/institutions, knowledge areas, clusters | Three distinct future sections; do not equate every dataverse with an institution |
| 6 | Scroll controls, keyboard, swipe, next-card cue, View all grids | Collections/knowledge phases; no autoplay |
| 3.2, 6 | Consistency across homepage and internal pages | Shared tokens, then narrow CSS/XHTML changes per template |
| 3.2, 6 | Identifiers, licenses, versions, citation and copy actions | Dataset/results phase; retain canonical metadata and permissions |
| 3.2, 6 | Informational/warning/critical dismissible notices | Style existing server-driven notices; validate current content first |
| 3.2, 6 | Readable dark footer, institutional and standards links | Footer phase; do not claim endorsement/certification from a reference logo |
| 5 | Blue/navy with restrained orange and optional yellow/blue | Provisional tokens; final palette approval pending |
| 5 | At most two Cyrillic-capable font families; base text at least 16px | Hero uses a system sans-serif stack; 16px controls/body minimum |
| 5 | 12 columns, content width 1200–1280px, 8px spacing rhythm | Hero max-width 1280px; shared shell grid in header/layout phase |
| 5 | Consistent SVG action icons; official institution logos | Hero actions use simple SVG icons; use supplied official logos for collections |
| 5 | Licensed/approved decorative WebP, at most 300KB | User-supplied image integrated as 59 KB WebP; provenance and responsive overlays recorded |
| 7 | 1440/768/375 layouts; intermediate widths; keyboard/reduced motion | Hero verification plus ongoing per-phase checks |
| 7 | WCAG 2.1 AA targets, contrast and 44×44 touch targets | Verify per component; no claim that the whole legacy application conforms |
| 8, 11 | Figma UI kit, prototype, asset licenses, developer handoff | Design deliverables tracked separately; repository docs are not a substitute |

## Page coverage and priorities

P1: homepage; collection/institution page; search and advanced search; result cards
for collection/dataset/file; dataset page. Each requires desktop, tablet and mobile
review. English homepage and dataset views are explicitly requested by the brief.

P2: View all grids; login/registration/account; data deposit/upload; information
pages; 404/500, maintenance, empty and cookie states. Existing native functionality
must remain usable while presentation is improved.

“Repository in numbers” is optional and needs approval plus genuine API values.
Do not add example statistics, placeholder organizations or fabricated data.

## Collection and content architecture

Use the native root featured-collections selection for a curated institutional
strip only after its actual entries are confirmed to represent institutions.
The endpoint returns ordered aliases; resolve published details through native APIs.
The root currently has no collections.

Knowledge areas and clusters need explicit models: editorial navigation to
collections, a metadata/search taxonomy, or curated mappings. The screenshot's
A/F/B/C/D order is an example, not a confirmed taxonomy. Do not silently create
collections or guess filter queries. Each card needs a stable key, localized
label, approved icon, order and valid destination. A collection may appear in
more than one section without duplicating its data.

Retain original dataset titles and authors. Localize UI/date/status formatting;
do not translate stored research metadata automatically. DOI versus Permalink
must reflect backend configuration and the object being displayed.

## Outstanding inputs

- Confirmation of the official English name and final palette.
- Five menu groups' actual destinations and submenu structure.
- Institution list, official logos and mapping to published collection aliases.
- Approved knowledge-area/cluster labels, order and taxonomy-to-search mapping.
- Visual approval of the integrated `hero1.png` background; source/provenance is recorded.
- DNTB identity and footer/editorial/support links.
- Staging accounts/test data for private, contributor and administrator states
  when those templates are redesigned.

The UaRD logo has been supplied; it is no longer an outstanding asset.
