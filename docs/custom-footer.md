# Custom footer — 2026-10-04

Uses the supported FooterCustomizationFile hook. No upstream XHTML changes.
Source files: customization/footer/template.html, footer.js, en.json, uk.json,
and links.json. Build using python3 scripts/customization/build-footer.py;
--check validates the generated customization/branding/custom-footer.html.
CSS is in the existing mounted custom-stylesheet.css.

Enable with the secured local admin API:

```bash
curl --fail-with-body -X PUT --data-binary '/var/www/dataverse/branding/custom-footer.html' http://localhost:8080/api/admin/settings/:FooterCustomizationFile
```

Custom blue columns render first via CSS ordering; the original #dvfooter renders
below as a compact copyright/privacy strip. Its logo/version column is hidden
only when the custom footer exists. Native copyright and conditional privacy
link remain server-rendered. Widget footer is excluded by the native hook and
CSS scopes. Removing the hook restores the default presentation.

The logo is the original supplied PNG, served unchanged from
/resources/images/uard/uard-logo.png. Footer JS is external and deferred;
see resource-assets.md for image/JS deployment requirements. Labels use native
Dataverse document locale; Ukrainian is the static fallback. Support reuses the
native headerSupportLink JSF action to open the existing contact dialog. The
button is omitted when that action is unavailable (e.g. external support URL).

Article/resource entries with null in links.json are non-interactive labels.
Replace null with a verified root-relative or HTTPS URL and rebuild to activate
an anchor. Current links are not articles and do not claim institutional
certification: the reference compatibility column is titled Open science ecosystem.
The initial footer-only step added no CMS, article routes or article content.
For later bilingual articles, prefer a single URL with server-side locale
selection; see the article guidance below.

Verified live at 1440/768/375/320px: no horizontal overflow, custom section above
native copyright row, hidden standard logo, support dialog opens, English labels
follow native selector, footer visible on native browse page, no JS errors.
Screenshots inspected. Native privacy policy is not currently configured, so its
visibility is preserved structurally but no configured-policy runtime check made.
Widget and authenticated role-based runtime verification remain outstanding.

## Page-bottom alignment fix

Pages with the custom footer use a full-height flex column; main content grows
on short pages, and the footer remains in flow on long pages. Footer JavaScript
removes the native template's whitespace-only body text containing an orphan
NBSP after parsing, which otherwise produces a 20px anonymous line below it.
This cleanup requires JavaScript; without it the footer remains readable but
the native spacer may remain. Widgets do not load this custom footer.
Verified the footer ends at document bottom (within pixel rounding) on desktop
and mobile homepages, native browse and a short login page with a tall viewport.
No horizontal overflow; support dialog still opens.

## First article link and bilingual articles (2026-10-11)

The About entry points to /about.xhtml, which selects the supplied Ukrainian or
English article server-side using Dataverse's native session locale. links.json
uses the string "/about.xhtml", and its bilingual label still follows the footer
translations. The former hreflang="uk" declaration was removed when the English
article was integrated. Other null entries retain their existing behavior.

links.json accepts null, a URL string, or a url/hreflang object (en/uk) for a
single-language destination. Prefer one stable URL without a fixed hreflang for
articles whose content follows the native session locale. Rebuild/check generated
HTML rather than editing it or copying the supplied footer fragment. Deploy the
article wrapper and both fragments before activating its footer link; see
[the About-page guide](custom-about.md#one-url-two-article-languages).

## Open-science ecosystem articles (2026-10-11)

EOSC, OpenAIRE, re3data and FAIR now link to /eosc.xhtml, /openaire.xhtml,
/re3data.xhtml and /fair.xhtml. Each destination is a url/hreflang="uk" object
because its supplied article is currently Ukrainian only, including in English
UI mode. The articles use the shared native shell and .uard-about styles. Footer
labels remain the existing acronyms. Other configured links and null entries
retain their behavior. Deploy all pages/fragments before publishing the generated
footer; see [ecosystem-pages.md](ecosystem-pages.md) for the manifest and future
English-localization pattern.

## Consistent link spacing and muted color (2026-10-11)

Activating ecosystem links exposed a difference between 44px anchor rows and
29px pending-text rows. All custom-footer column anchors, support buttons and
pending entries now share block-level flex layout, 4px vertical padding and a
29px minimum height on desktop. Multi-line labels can grow naturally. This removes
both the larger link spacing and the inline baseline gap while preserving the
native legal row and column-heading layout.

Links and support buttons use the same #b9ccdc color as pending text. Hover retains
white underlined text; keyboard focus retains its visible outline. At widths up
to 700px or with a coarse pointer, all three row types use a 44px minimum height
and 6px padding, keeping rows consistent and links comfortable to tap.

This is a stylesheet-only update: publish custom-stylesheet.css to its configured
branding path and refresh the browser/cache. No XHTML/JS rebuild, API change or
application restart is needed. Verified at 1440/768/375/320px: single-line rows match at 29px on desktop
and 44px on small screens; the longer support label wraps naturally at tablet
width. Link/pending/support colors match, white underlined hover and the keyboard
focus outline remain visible. A coarse-pointer 1024px context also retains 44px
tap targets. Article navigation and native support work, with no overflow or
JavaScript page errors. The desktop footer screenshot was visually inspected.

## DNTB operator articles (2026-10-11)

The operator-column Contacts and About DNTB entries now link to /contacts.xhtml
and /about-dntb.xhtml. Their links.json keys are contacts and aboutOperator, both
with hreflang="uk" because the supplied articles are Ukrainian in either native
UI language. Both use the existing shell and article styles; compact muted footer
row styling is retained. Deploy both wrappers/fragments before publishing the
new generated footer. See [operator-pages.md](operator-pages.md) for contact-link
formatting, deployment and future English content.

## Policies overview link (2026-10-11)

The policies entry now links to /policies.xhtml with hreflang="uk". It displays
the supplied publication-status introduction and nested overview of planned
policies in either UI language, using the native shell and article styles. The
existing compact muted footer link rules apply unchanged. Deploy its fragment,
wrapper and hierarchical-numbering CSS before publishing the generated footer.
See [policies-page.md](policies-page.md).
