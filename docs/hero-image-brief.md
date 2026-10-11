# UaRD hero background brief

The user supplied `hero1.png` and requested an integration trial on 2026-10-03.
That trial was superseded by the user-selected generated laptop-and-flag version,
without windows or domes: 30,356-byte WebP, 2172 × 724. See
`docs/hero-image-alternatives.md` for the current generation prompt and selection.
CSS provides a fallback and a responsive readability overlay. Any replacement
should likewise contain only the decorative background, never the whole hero UI. The official UaRD logo comes from
the supplied PDF and must not be generated or embedded in the background.

## Original prompt for ChatGPT (superseded; retained for provenance)

Create a wide, photorealistic website hero BACKGROUND for UaRD, the Ukrainian
National Research Data Repository. Canvas 2560 × 800 pixels, landscape. A bright,
quiet contemporary research workspace in Ukraine, with a silver laptop cropped
into the far right foreground, a softly blurred small Ukrainian blue-and-yellow
flag in the upper right background, and understated scientific network points and
fine connecting lines near the right third. Natural soft daylight, pale ice-blue
and white palette, subtle navy details, professional public research infrastructure
rather than a commercial technology advertisement. Keep the LEFT 65% almost empty:
a smooth very pale blue-to-white wash with low contrast and no distinct objects,
so dark blue live website text and a search field can be placed there. Place all
recognizable objects in the RIGHT 30–35%, with a gentle fade toward the center.
Keep the composition sparse, realistic and calm, with generous negative space.
No text, letters, numbers, logos, watermarks, interface elements, buttons, charts,
people, coats of arms or invented institutional branding. Do not recreate the
attached webpage; produce only its separate photographic background. If the exact
canvas size is unavailable, use the widest landscape format available and preserve
this composition for later cropping.

## Integration

1. Review the image at desktop and mobile sizes; keep text readable without
   depending on details in the image.
2. Record its source, creation date and applicable usage terms. The brief permits
   AI artwork with prior client notice; the user has explicitly requested this
   generation prompt. The original trial image was supplied by the user. The selected replacement was generated directly with the built-in Imagegen tool at the user's request.
3. Export an optimized WebP at no more than 300 KB (preferably below 250 KB).
4. Save it as `customization/homepage/assets/hero-background.webp`.
5. Run `python3 scripts/customization/build-homepage.py` and refresh the browser.
   The builder validates the WebP signature and size and embeds it in the fragment,
   so no new asset server or Maven build is needed.
6. Verify both languages at 1440, 768 and 375px; on mobile the artwork is decorative,
   so it stays faint behind the content. Use contain sizing to preserve the complete
   artwork at every width. CSS overlays protect foreground contrast.

The exact UaRD logo, search form, title and links stay as separate HTML/image
elements. Do not flatten them into the generated background.

## Current responsive treatment

- Desktop at 1280px and above: content capped at 760px within the existing 1280px
  grid; the right-hand photo remains visible beyond the protected text area.
- Below 1280px: a stronger uniform pale overlay prevents the laptop/flag competing
  with text and links. Background position is right-center at all widths; the image
  layer uses contain sizing without repeat to prevent cropping.
- Removed the former CSS dots and diagonal decoration, since the image already
  contains network graphics.
- The advanced-search label remains on one line at tested widths.
- Verified image decoding, both languages, native search and overflow at widths
  from 320 to 1920px. Final desktop, tablet and mobile crops were inspected.
