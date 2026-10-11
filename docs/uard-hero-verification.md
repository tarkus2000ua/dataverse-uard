# UaRD hero verification

Verified against local Dataverse 6.12 on 2026-10-03. This records the revised hero,
not an accessibility certification or validation of future homepage sections.

## Functional and layout checks

- Fresh browser session with English preference starts in Ukrainian.
- Actual JSF Ukrainian → English → Ukrainian selector actions work.
- Explicit English selection survives navigation to search and back.
- Official logo rendering loads at its original 400 × 400 aspect ratio.
- Basic search submits English, Ukrainian, ampersand and plus-sign input unchanged
  through native UTF-8 GET query encoding.
- Advanced search opens `/search/advanced.xhtml` and its JSF form.
- Browse collections opens `/dataverse.xhtml?types=dataverses`.
- Keyboard order: search input → submit button → Advanced search.
- Viewports 1440, 768, 375 and 320px have no horizontal overflow.
- Simulated 40% longer English UI/editorial strings at desktop/tablet/mobile do not
  create horizontal overflow.
- Native search works with JavaScript disabled; fallback hero text is Ukrainian.
- No JavaScript page errors during these interactions.
- Desktop/mobile source screenshots were inspected in both languages.
- The homepage generator consistency check and Git whitespace check pass.
- FRD completed for the server-side locale configuration; application API and
  interactive checks succeeded after deployment.

## Initial hero color checks

Historical CSS-background baseline, before the photographic integration below.
Ratios are calculated from the sRGB relative luminance formula. Decorative dots
use a conservative composited background of #d4eaf6 for the link check.

| Use | Foreground | Background | Contrast |
|---|---|---|---|
| Heading/body | #07376b | #eaf5fc | 10.73:1 |
| Search hint | #355775 | #eaf5fc | 6.84:1 |
| Action link | #0067ad | #eaf5fc | 5.35:1 |
| Action link over decoration | #0067ad | #d4eaf6 | 4.77:1 |
| Submit icon | #ffffff | #0067ad | 5.93:1 |
| Placeholder | #52687c | #ffffff | 5.79:1 |
| Input outline | #7693ae | #ffffff | 3.20:1 |

Hero body, labels, links and controls use at least 16px text. The submit button is
at least 48 × 48px; text links have at least 44px height. A strong outline marks
keyboard focus. No animation is used, and reduced-motion rules disable any future
inherited transition within the hero.

Recheck contrast and visual crops after adding photographic artwork. The existing
global navbar/footer and internal templates remain separate review work.

## Remaining review items

- Official English institutional name confirmation.
- Optional photographic background, final palette and visual approval.
- Broader application Ukrainian translations: current native navbar labels remain
  English because language-bundle activation/completion is separate from the hero.
- Screen-reader and authenticated/role-specific regression review when the shared
  shell is redesigned; current hero actions are public native navigation.

## Photographic integration check (2026-10-03)

- User-supplied `hero1.png` converted to WebP at quality 90, 2172 × 724,
  58,962 bytes (below the 300,000-byte build limit).
- Verified the live embedded WebP decodes with those exact dimensions.
- Removed the duplicate CSS network decoration.
- Checked Ukrainian/English, native search and layouts from 320 to 1920px.
- Final crops inspected at desktop and smaller desktop widths; tablet/mobile
  use a stronger overlay. Advanced-search label fits on one line.
- Action text sits within the 96% pale overlay on full desktop, or the 94%
  uniform overlay below 1280px. Even compositing that 94% overlay against black
  gives approximately #dce6ed, with #0067ad action text at 4.68:1.
- No JavaScript errors or horizontal overflow observed.
- Generator consistency and Git whitespace checks pass; branding refresh only,
  no additional FRD redeploy required for this image integration.

## Selected laptop-and-flag background (2026-10-03)

Supersedes the original photographic trial above. The user selected generated
`hero-laptop-flag-v2.webp`; the live embedded asset decodes at 2172 × 724 and
30,356 bytes. Image sizing changed from cover to contain, with no-repeat and
right-center positioning. The gradient remains a separate full-banner layer.

- Inspected Ukrainian desktop screenshots at 1440/1280px and mobile at 375px.
  Laptop and flag are inside the banner; mobile decoration remains subdued.
- No horizontal overflow at 1440, 1280, 768 or 375px.
- Fresh English-preference browser session starts Ukrainian; native selector
  switches hero to English. English desktop screenshot captured.
- Native search preserves `water & вода + 2026`; no JavaScript page errors.
- Existing contrast overlays retained unchanged.
- Homepage generator consistency and Git whitespace checks pass.
- Branding refresh only; no upstream source changes or FRD needed.
