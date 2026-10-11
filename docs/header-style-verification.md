# Header styling — 2026-10-03

CSS-only first pass in customization/branding/custom-stylesheet.css.
The real dataverse_header.xhtml remains unchanged, including every permission,
authentication, notification, locale and action binding. Existing Dataverse brand
and real navigation destinations remain in place pending institutional navigation
and brand hierarchy decisions.

## Presentation

- White 64px fixed navbar, navy 16px navigation, 1280px content alignment.
- Minimum 44px controls, explicit focus outlines, rounded search/dropdown panels.
- Bootstrap collapse at widths below 1280px to accommodate longer navigation.
  Expanded menus scroll within the available viewport and dropdowns flow inline.
- Account name can truncate visually without removing its accessible text;
  notification badges and the superuser color remain intact.
- Header spacer matches the 64px navbar; widget-view spacing is unchanged.
- Homepage gap was the 14px top margin on an empty messagePanel. Only a homepage
  panel with no child elements loses that margin. Real message/alert markup is
  preserved, as is spacing on internal pages.

## Verification

Local Dataverse 6.12, isolated signed-out Chrome session:
- Closed navbar touches hero exactly (0px gap) at 1440, 1280, 1024, 768, 375, 320px.
- No horizontal overflow at these widths, including expanded mobile search.
- Mobile collapse opens/closes after Bootstrap transitions; all seven current
  navigation entries remain available. Desktop/mobile screenshots inspected.
- Native locale selector switches to English.
- Native navbar search submits to existing Dataverse search; internal results
  page has no horizontal overflow.
- Support action opens the existing dialog (no message sent).
- No JavaScript page errors observed.

No authenticated credentials/session were used: account, unread notifications,
superuser dashboard and permission combinations still require live role-based QA.
Source bindings are unchanged, but CSS-only changes do not constitute that QA.
Native Ukrainian bundle activation/review, institution branding and the five
proposed navigation groups remain follow-up work. No fake destinations added.

Mounted CSS refresh only; no FRD or upstream source changes for this pass.
