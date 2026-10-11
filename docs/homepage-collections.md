# Homepage featured collections

Implemented 2026-10-03 using the existing featured-dataverses curation model.
No new endpoint, API credential, upstream template edit or carousel library.

## Data and curation

The homepage anonymously fetches `/api/dataverses/:root/featured`, then resolves
aliases with `/api/dataverses/{alias}`. Cards preserve list order, link to native
`/dataverse/{alias}` pages, and display only successfully resolved published
collections. API requests omit credentials and time out after 10 seconds.
Names are inserted as text, not HTML. Contact emails are not rendered.

Manage the root featured list using the existing Dataverse admin UI or the
collection API. Refresh the homepage to see changes. This renders the same list
as the built-in JSF carousel, without importing that carousel into raw custom HTML.
The example-data JSON and creation script do not drive the live homepage.

The current five featured collections are institutions. If the root featured
list later includes other collection types, either curate an institution-only
list or revise the section heading; there is no silent type filter.

## Presentation and localization

CSS scroll-snap strip with visible next-card cue, native overflow/touch scrolling,
previous/next buttons, position text, keyboard arrow keys on the focused list,
and normal Tab navigation through card links. No automatic rotation. Reduced
motion uses immediate button scrolling. Button states reflect the scroll edges.

UI strings are in homepage locales/en.json and uk.json; JSF remains the locale
source. Collection names remain the stored Dataverse names in both languages.
Official logos and approved bilingual institutional labels remain follow-ups;
current cards use a neutral Dataverse collection icon, not fabricated logos.
View all opens native `/dataverse.xhtml?types=dataverses` as the interim directory.

Loading, empty, failure, partial-result and retry states are translated. A
no-JavaScript fallback directs visitors to the native all-collections link.

## Files and workflow

- customization/homepage/template.html: section, list, controls and fallback.
- customization/homepage/homepage.js: public API loading and navigation.
- customization/homepage/locales/{en,uk}.json: labels and status messages.
- customization/branding/custom-stylesheet.css: scoped strip styles.
- customization/branding/custom-homepage.html: generated mounted artifact.

Run `python3 scripts/customization/build-homepage.py` after source/translation
changes, then refresh. No FRD needed.

## Verification

Live Chrome checks: five API-backed cards in configured order; no page overflow
at 1440, 768, 375 and 320px; previous/next and keyboard scrolling at each width;
English heading after native locale action; card opens `/dataverse/dntb`.
Desktop and mobile screenshots inspected. Simulated empty, HTTP 503 and partial
collection failures showed appropriate states; retry recovered to the live list.
No page errors observed. Real-device touch and screen-reader review remain open.

## Reference-layout revision — 2026-10-04

Adjusted to the supplied cropped design: generic institution SVG beside the
heading, shorter View all label, five complete desktop cards, fine pale borders,
and 44px circular chevrons vertically centered at the sides. Position text is
retained for assistive technology but visually hidden. Mobile/tablet show a
partial next card; the scrollbar is hidden while native scrolling remains active.
No institutional logos were extracted or fabricated; neutral icons remain.

Verified live five-card fit at 1440/1280px, disabled desktop arrows when there is
no overflow, working arrows at 768/375/320px and no page overflow at any tested
width. Desktop/mobile screenshots inspected. Existing API logic is unchanged.

## Scroll alignment fix — 2026-10-04

All three strips now share card-boundary arrow movement, rather than scrolling
an arbitrary 80% pixel distance. CSS scroll-padding matches the 4px list inset,
preserving the first visible border and making scroll position zero a snap point.
Static-strip regression checks at 1440/1024/768/375/320px verify right/left round
trips, visible left borders, reaching both ends and disabled edge buttons.
Smooth-motion desktop round trip also checked.
