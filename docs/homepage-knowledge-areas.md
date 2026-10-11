# Knowledge-area strip — 2026-10-04

Current update (2026-10-12): all cards now have native filtered-browse destinations
from the separate link mappings. See [homepage-navigation.md](homepage-navigation.md).
The following 2026-10-04 notes describe the initial placeholder implementation.

Added all eleven A–K labels supplied by the user in English and Ukrainian.
Editorial labels live in customization/homepage/content/knowledge-areas.{en,uk}.json;
UI headings, status and arrow labels remain in locales/{en,uk}.json. The builder
validates A–K ordering and generates Ukrainian fallback cards plus bilingual data.
Uses the existing blue 64px folder SVGs and 32px book heading icon.

Cards deliberately have no links or click handlers. The pending-links note was
removed at the user's request. A translated View all link beside the heading opens
`/dataverse.xhtml?types=dataverses` as an interim collections browse destination. No production URL, subtree ID or language-specific
Solr query from the supplied example has been activated. Future destinations
require a verified collection alias, taxonomy facet values and subtree mappings.
The supplied URL is a future format example, not a current navigation target.

The second strip shares the institution strip layout and side-arrow styles, with
independent scrolling, edge states, keyboard arrows and reduced-motion handling.
The static list remains readable and scrollable without JavaScript. No new API,
upstream change, external request or FRD is needed.

Verified 11 cards, no placeholder anchors, no horizontal page overflow and working
scroll buttons at 1440/768/375/320px; English A/K labels after native locale switch;
no JavaScript errors. Desktop/mobile screenshots captured.
