# UaRD short system article (2026-10-11)

The top-menu About entry opens /system.xhtml. This is a separate short article
from the longer bilingual /about.xhtml article linked from the footer.

## Sources and language behavior

- src/main/webapp/system.xhtml: wrapper using dataverse_template.xhtml.
- src/main/webapp/uard/content/system.uk.xhtml: supplied Ukrainian editorial text.
- customization/branding/custom-stylesheet.css: existing .uard-about styles, unchanged.

The supplied two paragraphs, dates, project registration number and operator
wording are preserved. The operator label is bold and its DNTB link points to
https://dntb.gov.ua/. The page reuses the pale-blue heading, readable article column,
paragraph spacing and return-home link from the About page. No English article
was supplied, so both native interface languages render the Ukrainian fragment.
The article has lang="uk"; title and description also remain Ukrainian. The native
header/footer follow Dataverse's selected language, and the link stays available
in both languages, as authorized by the user. No empty English placeholder is shown.

The wrapper currently includes /uard/content/system.uk.xhtml directly. Once English
text is supplied/reviewed, add system.en.xhtml and use the same server-side locale
selection pattern as [the About article](custom-about.md#one-url-two-article-languages):

```xhtml
<ui:include src="#{dataverseSession.localeCode eq 'en'
                  ? '/uard/content/system.en.xhtml'
                  : '/uard/content/system.uk.xhtml'}" />
```

Also localize the page title and description at that time. Keep one /system.xhtml
URL and the existing Dataverse session locale; do not introduce a separate language
preference. Long editorial content stays in fragments, not native UI bundles.

## Native top-menu hook

The existing dataverse_header.xhtml already renders an About entry when
:NavbarAboutUrl is configured. Its text uses the native header.about bundle key;
its normal new-tab behavior and authentication/navigation logic are preserved.
No header XHTML, Java, footer links, language settings or translation files were changed.
The local setting is configured only after the page is available:

```bash
curl --fail-with-body -X PUT --data-binary 'http://localhost:8080/system.xhtml' \
  http://localhost:8080/api/admin/settings/:NavbarAboutUrl
```

This setting is stored in the local database, not transported by Git or XHTML.
The local native Ukrainian UI bundles remain incomplete; article integration is
not a full navbar translation. If the remote menu should say "Про систему", the
administrator can set header.about=Про систему in the site's maintained Ukrainian bundle,
using its existing translation deployment/cache procedure. Do not replace the
remote bundle with the local incomplete files.

## Direct Payara deployment

With the preceding UaRD/About styling baseline already installed, deploy:

1. src/main/webapp/uard/content/system.uk.xhtml to
   <active-deployed-app>/uard/content/system.uk.xhtml.
2. src/main/webapp/system.xhtml to <active-deployed-app>/system.xhtml.
3. Verify https://uard.dntb.gov.ua/system.xhtml, then configure the native About hook.

Copy the fragment before the wrapper. Confirm the active application root beside
dataverse_template.xhtml and WEB-INF, and make the files readable by the Payara
service user. Include them in the maintained deployment artifact. For packaged
WAR installations, include them in the normal rebuilt artifact. If copied views
are not discovered, check paths/permissions and reload/redeploy the application
through the site's normal procedure. A full server restart is not automatically
required. Local development uses ./scripts/dev/dev-frd.sh.

Run the following through the remote server's existing secure admin procedure,
using its actual local HTTP port. Read/record any existing About URL before replacing
it. The public target URL belongs in the setting; the localhost admin endpoint
below refers to the remote server when run from its SSH console.

```bash
LOCAL_URL='http://localhost:8080'
curl --fail-with-body "$LOCAL_URL/api/admin/settings/:NavbarAboutUrl"
curl --fail-with-body -X PUT \
  --data-binary 'https://uard.dntb.gov.ua/system.xhtml' \
  "$LOCAL_URL/api/admin/settings/:NavbarAboutUrl"
```

An absent existing setting returns 404; that means the native About item was hidden.
No CSS/JS/footer or language changes are required for this incremental update when
the existing UaRD baseline is present. Remote deployment was not performed.

## Verification

The two XHTML files parse as XML, and supplied paragraph/label/link text is checked
for preservation. Local FRD succeeded. The configured native About link opened /system.xhtml with
its normal new-tab behavior. UA -> EN -> UA kept the supplied Ukrainian article,
title and description while the native UI locale changed. Exactly one article,
header and custom footer rendered. No horizontal overflow occurred at
1440/768/375/320px; desktop and mobile screenshots were visually inspected.
The operator link and bold label matched the supplied text. Native support
opened/closed; return-home and the separate long bilingual footer article worked.
The system article rendered without JavaScript, and no JavaScript page errors
were observed. XML, both branding builders --check, and git diff --check passed.
Native header, existing CSS/footer, unrelated files and the Git index were preserved.

## Empty-header-panel spacing fix

The fixed navbar is 64px high; #dataverse-header-block keeps its 64px top padding
to reserve that space. The native empty message panel added a separate 14px top
margin on article pages. The existing homepage-only CSS exception now includes
.uard-about, which is shared by /about.xhtml and /system.xhtml. It sets margin-top
only on message panels with no child elements; panels containing real messages
retain their native spacing. Native dataset/collection pages and widgets remain
outside this scoped exception.

Deploy the updated customization/branding/custom-stylesheet.css to the configured
remote branding path and refresh the page/cache. This CSS fix needs no XHTML,
JavaScript, API setting changes or application redeployment. Verified locally: navbar bottom and hero top both align at 64px on
About, System and homepage at 1440/768/375/320px, with no overflow. The 64px
header padding remains; a temporary nonempty message panel retained its 14px
margin and visibility. Native login-page spacing and widget exclusion were also
checked. No JavaScript page errors occurred; the corrected screenshot was inspected.
