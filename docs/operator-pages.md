# DNTB operator footer articles (2026-10-11)

| Footer entry | Public route | Editorial fragment |
| --- | --- | --- |
| Contacts / Контакти | /contacts.xhtml | uard/content/contacts.uk.xhtml |
| About DNTB / Про ДНТБ | /about-dntb.xhtml | uard/content/about-dntb.uk.xhtml |

Application sources live under src/main/webapp. Each route is a Facelets wrapper
using dataverse_template.xhtml and a separate Ukrainian editorial fragment, with
existing .uard-about styles. Native header/footer and session language switching
are retained. The supplied articles and their title/description remain Ukrainian
in either UI locale until English copy is supplied; article lang and footer
hreflang are uk. No independent frontend language preference is added.

The contact page preserves the institution name, address, displayed phone number,
email and website. Labels are bold and the details use line breaks. Email uses
mailto:dntb@dntb.gov.ua and the website uses https://dntb.gov.ua/. The displayed
(044) 521-93-52 phone number links to tel:+380445219352 for supported clients.
The About DNTB page makes the bold institution name a link to https://dntb.gov.ua/;
the user's parenthetical instruction to add a link is implemented as markup,
not displayed as article text. Each page has the standard return-home link.

## Editing and English localization

Edit the *.uk.xhtml editorial fragments. When reviewed English copy is ready,
add corresponding *.en.xhtml fragments and follow the one-URL locale-selection
pattern in custom-about.md. Localize title/description and remove the fixed uk
footer destination language when each route becomes bilingual. Preserve native
locale settings and keep long editorial text separate from native UI bundles.

Footer destinations are contacts and aboutOperator in customization/footer/links.json.
Generate customization/branding/custom-footer.html with build-footer.py and verify
with --check; do not edit the generated fragment directly. Existing compact muted
footer link styles are reused without CSS changes.

## Deployment

Local application source changes use ./scripts/dev/dev-frd.sh. Check the two routes
before generating/publishing the mounted footer links.

On direct Payara with the UaRD baseline installed:

1. Copy src/main/webapp/uard/content/contacts.uk.xhtml and about-dntb.uk.xhtml to
   matching paths under <active-deployed-app>/uard/content/.
2. Copy src/main/webapp/contacts.xhtml and about-dntb.xhtml to the active application
   root beside dataverse_template.xhtml and WEB-INF.
3. Check both public pages, reloading/redeploying the application through the site's
   normal procedure if it does not discover the views after checking paths/permissions.
4. Publish the generated custom-footer.html to the existing configured branding path.

The application user must be able to read the files. Preserve the directory
structure and include all four XHTML files in the maintained deployment artifact.
Packaged installations require these files in the normal rebuilt WAR. No admin
settings, native translation bundles, JavaScript or CSS changes are needed for
this feature when the preceding UaRD/footer-style baseline is installed.

The generated footer also references the four ecosystem pages from the preceding
update, so they must be deployed too. The combined footer-article package contains
all six Ukrainian pages (12 XHTML files), the generated footer and current stylesheet
so the administrator can deploy the pending pages together. Preserve any remote
stylesheet additions when merging an older style baseline. Remote deployment was
not performed by this task; a full Payara restart is not automatically required.

## Verification

All four XHTML files parse as XML. Local FRD and both branding builders --check succeeded. Both actual footer links
opened their pages and return-home links worked. The supplied contact values,
bold labels, three line breaks, tel/mailto/website hrefs and linked institution
name were checked; tests did not initiate calls or send email. UA -> EN -> UA
retained the Ukrainian article/metadata and native interface language selection.
Native support and keyboard focus worked, and both pages/footer links rendered
without JavaScript. Layout checks at 1440/768/375/320px had no overflow or header
gap; operator links retained the muted color and 29px desktop/44px mobile rows.
Desktop Contacts and mobile About DNTB screenshots were visually inspected.
No JavaScript page errors occurred. XML and git diff --check passed; unrelated
pending files, native header, stylesheet, JS resources and Git index were preserved.
