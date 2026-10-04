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
A language-specific article URL mapping will be needed when pages are created.
No CMS, article routes or article content was added in this step.

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
