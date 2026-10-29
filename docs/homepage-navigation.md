# All homepage taxonomy links: incremental remote test

All 11 knowledge-area cards and all 15 cluster cards now link to native filtered
browse within /dataverse/uard, retaining the object-type filter and date sorting.
The successfully tested A Education URL is unchanged.

On 2026-10-11, the remote public metadata-block API was read without making changes:
https://uard.dntb.gov.ua/api/metadatablocks/knar
https://uard.dntb.gov.ua/api/metadatablocks/clusters
All 11 English letter-plus-label knowledge-area texts and all 15 English cluster
texts exactly match their corresponding controlled vocabulary values, in order.
The fields are knarSubject and clustersSubject. Native Dataverse facetable Solr
fields use the _ss suffix, so URLs filter knarSubject_ss and clustersSubject_ss.

Both native UI languages use those same canonical English filter values.
Visible Ukrainian/English labels follow the existing native locale integration.
No display labels, JavaScript, application resources, CSS or settings changed in
this extension. Authentication, permissions, support and switching remain native.
The View all links retain their existing destinations.

Editable destinations:
- customization/homepage/content/knowledge-area-links.json
- customization/homepage/content/cluster-links.json
Keep filters separate from localized labels; confirm stored vocabulary values
before updating a mapping. Do not derive filters from translated Ukrainian labels.
Build: python3 scripts/customization/build-homepage.py, then --check.
All cards are generated native anchors and are present without JavaScript.

Remote installation after the successful A-card test:
1. Back up the existing homepage branding fragment.
2. Replace only the configured HomePageCustomizationFile with custom-homepage.html
   from this package (normally /var/www/dataverse/branding/custom-homepage.html).
3. Refresh browser/relevant cache. Keep the linked-card styling installed for
   the prior A-card test. No extra stylesheet, WAR, JS, metadata or setting changes
   are required for this extension.

Verify representative knowledge-area and cluster cards in both native languages.
The filter value should match the corresponding dropdown option and a known
published matching dataset. An empty result can be legitimate if no accessible
published datasets have that value. Check keyboard Tab/Enter and mobile scrolling.
Rollback: restore the prior homepage HTML.

Verified locally: generated and live HTML each contain 11 knowledge-area and
15 cluster anchors; every decoded query matches its field/value and the retained
scope/types/sort parameters. Existing hover/focus rules are served locally.
All English card texts match the remote API's controlled vocabulary exactly.
Both builders --check and git diff --check pass.
Staged dummy header, unrelated deployment edits and other pending work are preserved.

The local metadata blocks do not contain the remote custom fields and the local
root alias is root. Filtered dataset results and interactive browser navigation
remain for remote verification. The remote HTML browse page returned HTTP 403 to
automated read-only access; its public metadata APIs were readable.
Remote deployment was not performed. This package requires the matching UaRD
homepage resources and the linked-card CSS already used in the successful A test.

## Hero repository button (2026-10-25)

The former parenthetical Dataverse attribution below the hero title is replaced
by a blue repository navigation button. Ukrainian: “Перейти в репозитарій”;
English: “Go to repository”. Its native /dataverse.xhtml destination opens the
root repository browse page without an object-type filter or a hardcoded alias.
It is a normal anchor, usable without JavaScript, with a visible keyboard focus
outline and a minimum 44px height.

Edit template.html and locales/{en,uk}.json under customization/homepage; regenerate
customization/branding/custom-homepage.html with build-homepage.py. Publish that
fragment and the matching custom-stylesheet.css together. JavaScript, application
resources and settings are unchanged; mounted local branding refreshes directly.

Verification: both branding builders --check and git diff --check pass. An isolated
preview of the generated fragment and stylesheet verified Ukrainian/English labels,
native target href, keyboard focus, static fallback without the homepage script,
46px button height and no page overflow at 1440/768/375/320px. Desktop and mobile
screenshots were inspected. This preview excludes the native JSF shell and uses
an empty featured-list fixture; it is not a live Dataverse navigation test.

Live local verification on 2026-10-25 confirmed navigation through the new
button to the native repository browse page, existing featured collections/logos,
UA -> EN -> UA switching and desktop/mobile layouts. No remote deployment or
settings changes were performed for this homepage update.
