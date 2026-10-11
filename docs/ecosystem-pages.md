# Open-science ecosystem footer articles (2026-10-11)

Four articles use the native Dataverse shell and existing .uard-about styling:

| Footer entry | Public page | Editorial fragment |
| --- | --- | --- |
| EOSC | /eosc.xhtml | uard/content/eosc.uk.xhtml |
| OpenAIRE | /openaire.xhtml | uard/content/openaire.uk.xhtml |
| re3data | /re3data.xhtml | uard/content/re3data.uk.xhtml |
| FAIR | /fair.xhtml | uard/content/fair.uk.xhtml |

All application paths are relative to src/main/webapp. Each wrapper uses
/dataverse_template.xhtml and includes its editorial fragment. The page title,
description and article are Ukrainian. The native header/footer still follow
Dataverse's selected interface language. Both interfaces show the Ukrainian
article until English text is supplied. Each article is explicitly lang="uk";
footer links carry hreflang="uk" using the builder's existing destination object.

The user-supplied titles, paragraphs, external link labels, destinations and link
annotations are preserved as editorial copy. Links are formatted as a semantic
list using the existing article styles. Each article has a return-home link.
No English text was fabricated, and no changes were made to native JSF shell,
authentication, permissions, locale handling, Java, CSS or JavaScript sources.

## Editing and future English versions

Edit the appropriate src/main/webapp/uard/content/*.uk.xhtml fragment for article
text. Keep native UI translations separate from longer editorial content. Once
reviewed English content is available, add a corresponding *.en.xhtml fragment
and use the shared server-side language-selection pattern in custom-about.md.
Localize title and description metadata at the same time. Retain one URL per
article, and remove the fixed Ukrainian hreflang object from its footer destination
when the route becomes bilingual. Do not introduce a separate language store.

Destinations are configured in customization/footer/links.json. Generate footer
HTML with scripts/customization/build-footer.py, then use --check to validate.
Do not edit customization/branding/custom-footer.html directly. Deploy wrappers
and content fragments before publishing the footer HTML that links to them.

## Local deployment

Run ./scripts/dev/dev-frd.sh for the new XHTML files. Check all four routes, then
run python3 scripts/customization/build-footer.py to activate the mounted footer
links. No setting changes are needed when FooterCustomizationFile is configured.

## Direct Payara deployment

With the existing UaRD branding/resources and article styles installed:

1. Copy all four src/main/webapp/uard/content/{eosc,openaire,re3data,fair}.uk.xhtml
   fragments to the same paths under <active-deployed-app>/uard/content/.
2. Copy src/main/webapp/{eosc,openaire,re3data,fair}.xhtml to the root of the active
   Dataverse application, beside dataverse_template.xhtml and WEB-INF.
3. Check all four public URLs; reload/redeploy the application through the normal
   procedure if copied views are not discovered after checking paths/permissions.
4. Publish the generated custom-footer.html to its existing configured branding
   path (normally /var/www/dataverse/branding/custom-footer.html).

The application user must be able to read all files. Preserve directory structure
and include the eight XHTML files in the maintained deployment artifact so later
redeployments retain them. A packaged deployment needs the files in its normal
rebuilt WAR. No new admin setting, native language bundle or resource JS changes
are required. The deployment archive also includes the current integrated stylesheet
for sites that still need the preceding article styles/header-gap fix; if that
baseline is already installed, it does not need replacing for this feature.
Preserve remote stylesheet additions when merging an older baseline.

A full Payara restart is not automatically required for copied XHTML files.
This task deploys locally; no remote installation is performed. Footer links are
root-relative, consistent with the existing site-root deployment assumptions.

## Verification

All eight XHTML files parse as XML. Article paragraphs and all eight external
link labels/destinations were checked against the supplied content. Local FRD redeployment and both branding builders --check succeeded. Each of the
four footer links opened its matching page and returned home successfully. Both UI
languages displayed the supplied Ukrainian article, metadata and links. All four
pages had one article, native header and custom footer, zero navbar-to-hero gap
and no overflow at 1440/768/375/320px. Native support opened/closed, an article link
received keyboard focus, and all pages/footer links rendered without JavaScript.
No JavaScript page errors occurred. EOSC desktop and re3data mobile screenshots
were visually inspected. XML/source-text/link checks and git diff --check passed.
Unrelated staged/working files and the Git index were preserved.
