# Clusters strip — 2026-10-04

Current update (2026-10-12): all cards now have native filtered-browse destinations
from the separate link mappings. See [homepage-navigation.md](homepage-navigation.md).
The following 2026-10-04 notes describe the initial placeholder implementation.

Third homepage strip with 15 labels supplied in English and Ukrainian, preserving
input order. Editorial source: customization/homepage/content/clusters.{en,uk}.json.
UI strings remain in locales/{en,uk}.json. The builder checks matching ordered
keys, nonempty labels and safe icon keys; Ukrainian markup is the no-JS fallback.

Original single-blue 64px SVG symbols in assets/icons/clusters/ distinguish each
cluster. The existing 32px connected-node SVG is the section heading icon.
Five cards across desktop; responsive side controls, native overflow, keyboard
arrows and reduced-motion handling match the knowledge-area strip. Both static
strips now use one shared scrolling initializer.

Cards have no destinations yet. View all uses native collections browse as an
interim destination, matching the knowledge-area section. No production taxonomy
queries are activated. No external API, upstream modification or FRD required.

Verified 15 cards, knowledge/cluster button scrolling and no page overflow at
1440/768/375/320px; English first/last labels after native locale switch; no page
errors. All 15 icons and the desktop strip visually inspected. Future work:
verified destination mappings and review of the representative icon choices.
