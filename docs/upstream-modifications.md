# Upstream modifications

This file records intentional changes to Dataverse 6.12 source and their upgrade
review points. Branding HTML/CSS, translations and assets under `customization/`
are maintained separately.

## Deposit Date range validation

`DatasetVersion` checks new and changed `dateOfDeposit` values in the metadata
form and dataset version write commands. It accepts calendar dates between
1900-01-01 and today in Europe/Kyiv, inclusive. The form uses the same time zone
when filling in a default. Year and year-month values are checked at their
entered precision. Updates compare the submitted date with
the previous version so an unchanged legacy date does not block unrelated edits.
The form displays the error by the field and the Native API returns a bad request.
The message is in the packaged English and external English/Ukrainian bundles.

On upgrade, recheck form and Native API saves, including partial dates and edits
to a draft with a preexisting out-of-range value.

## Hidden My Data role filters

`src/main/webapp/mydata_fragment.xhtml` retains the original Roles heading and
checkboxes inside `ui:remove`, so Facelets omits them from the rendered page.
Hidden `role_ids` inputs use the same `MyDataPage.roleInfoForCheckboxes` list to
preserve the default all-roles search; the API rejects a request without roles.
Object-type, publication-status and metadata-validity filters, result-card role
badges, authentication and permission checks retain their native behavior.
The `mydataFragment.infoAccess` help text in the packaged English fallback and
external English/Ukrainian bundles no longer advertises filtering by roles.

Deploy through the normal application update (local development:
`./scripts/dev/dev-frd.sh`). On upgrade, verify that hidden role inputs still
serialize with the filters and searches return the same accessible items. To
restore the controls, remove the `ui:remove` wrapper and the hidden-input repeat
together to avoid submitting duplicate role IDs, and restore the help text.

## Author identifier choices in metadataFragment.xhtml

The native author identifier dropdown offers ORCID, ROR, ResearcherID and
ScopusID. Its JSF select-items expression filters only `authorIdentifierScheme`;
ResearcherID uses its canonical name regardless of the UI/metadata language.
Other controlled vocabularies retain their original choices and translations.

An existing identifier outside this list remains available in its own author row
so editing unrelated metadata does not clear it or fail JSF validation. New rows
offer only the four preferred schemes. The database vocabulary, metadata-block
TSV, API imports/exports, converter, value binding and permissions are unchanged.
Commenting out TSV entries would not hide values already loaded in the database.

Deploy this XHTML through the normal application update (local development:
`./scripts/dev/dev-frd.sh`); no metadata reload or database migration is required.
On upgrade, recheck the compound-field single-select component, new/repeated
author rows, existing legacy selections, and ResearcherID in both UI languages.

## Keyword vocabulary dropdown

`datasetFieldForEditFragment.xhtml` uses the standard PrimeFaces dropdown to
offer UNESCO Thesaurus, AGROVOC and MeSH in each Keyword row. Changing the selection fills the
matching URL in that row. Depositors enter the term and term URI separately.
The vocabulary is optional, and a previously stored custom vocabulary remains
selectable in its own row. The existing field binding, validation and metadata
schema still apply. The dropdown only renders when the name field is not
managed by an external vocabulary configuration.

`metadataFragment.xhtml` associates the field label with the dropdown. Deploy
both fragments together. On upgrade, verify keyword rows added through AJAX,
legacy vocabulary names and the URL written by each choice.

## src/main/webapp/WEB-INF/faces-config.xml

Status: implemented for the UaRD hero revision on branch `codex/homepage-hero`.

Reason: the user confirmed Ukrainian as the first-visit language. The standard
JSF default was English, with French and Chinese browser-negotiated supported
locales, independent of the installation's English/Ukrainian selector.

Change:
- Set the JSF default locale to `ua`, matching the installation's current legacy
  `:Languages` setting and `Bundle_ua.properties` filename.
- Remove the upstream French/Chinese initial-locale negotiation declarations.
  Do not add English as a browser-negotiated locale: the requested first-visit
  default is Ukrainian even when Accept-Language is English.
- Explicit English/Ukrainian choices still use the unchanged DataverseSession
  action and session/view locale. The homepage maps legacy `ua` to standard `uk`.

Assumptions: this deployment currently offers English and Ukrainian. The existing
legacy `ua` code is not the standard Ukrainian BCP 47 language tag; a future
migration to `uk` must coordinate selector settings, bundle filenames and JSF
configuration. Do not rename only one layer.

Verification: fresh English-preference browser starts with document locale `ua`
and hero locale `uk`; selecting English survives navigation; switching back works.
The change was deployed using `./scripts/dev/dev-frd.sh`.

Upgrade notes: recheck JSF locale negotiation and any new Dataverse default-locale
setting. Prefer a supported deployment setting if a future release adds one.
Preserve the explicit user-selection behavior and test first visits with multiple
Accept-Language values. No Java authentication/locale code was changed.

## Historical header experiment

`src/main/webapp/dataverse_header.xhtml` previously had a deployed local-dev
attribute and temporary brand text to verify FRD. At this revision its source
matches v6.12; the FRD redeploy replaced the stale deployed experiment with that
source. Header styling/restructuring is planned, not part of the hero revision.

When modifying the header later, record its exact presentation changes here and
preserve permissions, actions, component IDs, language, support, account and
notification behavior.

## Added resources/images/uard and resources/js/uard

UaRD images/SVGs moved to a dedicated application resource folder; generated
JavaScript is under resources/js/uard. No pre-existing upstream assets or JSF
templates were changed. Branding builders reference absolute versioned URLs.
Preserve these added directories during upgrades and regenerate scripts from
customization sources. See resource-assets.md for deployment ordering.

All three homepage heading icons now load from resources/images/uard/icons/,
including institutions.svg extracted from the homepage fragment. The institution
icon retains its original 32px geometry, blue color and three paired columns;
the branding builder generates its versioned image URL. No upstream template
or application logic was changed for this extraction.

The generated UaRD homepage script loads original institution logos from native
`/logos/` URLs using public collection theme metadata and parent theme IDs.
The existing `/api/access/dvCardImage/{id}` endpoint is a fallback, followed by
the generic icon. Logos use the native featured-strip 160px by 60px limits.
No Java or JSF templates changed. See resource-assets.md for details.

## Added src/main/webapp/about.xhtml (2026-10-11)

First footer article, integrated from the user-supplied Facelets page. The new
composition uses dataverse_template.xhtml and its existing navbar, language
selector, support dialog and custom/native footer. showDataverseHeader=false
suppresses the collection banner, not the application navbar. No existing JSF
shell or Java source was changed.

The page now follows the documented bilingual editorial pattern. The wrapper
selects src/main/webapp/uard/content/about.en.xhtml for native locale en, or
about.uk.xhtml for ua/uk (and the current Ukrainian fallback). Both fragments use
the original .uard-about layout and section IDs. Ukrainian article markup is
preserved; English paragraphs and list entries are from the user-supplied text.
Only one article renders. Page title and description metadata are localized too.

The footer continues to link to /about.xhtml in both UI languages; the fixed
hreflang="uk" declaration was removed. No existing JSF shell, locale/authentication
logic, Java, CSS or generated JS resources were changed for this bilingual update.
Preserve the wrapper and both fragments during upgrades, and deploy them together.
See custom-about.md for the reusable pattern, deployment and verification.

## Added short system article (2026-10-11)

Added src/main/webapp/system.xhtml and uard/content/system.uk.xhtml. The wrapper
uses the native template, and the fragment reuses .uard-about styling. Supplied
Ukrainian text is displayed in either native UI language until English content is
provided. The article is explicitly lang="uk". The long bilingual footer article
at /about.xhtml remains separate.

The existing :NavbarAboutUrl hook connects the top menu to /system.xhtml; the
setting must be configured per environment. No native header/footer source,
Java, CSS, language bundles or locale settings were modified. Preserve both
added files during upgrades; see custom-system.md for deployment and future
English localization.

## Added ecosystem article wrappers and fragments (2026-10-11)

Added eosc.xhtml, openaire.xhtml, re3data.xhtml and fair.xhtml under src/main/webapp,
with corresponding uard/content/*.uk.xhtml editorial fragments. The wrappers use
dataverse_template.xhtml; no native template or Java code was modified. User-supplied
Ukrainian text appears in either UI locale with article lang="uk". All pages reuse
existing styles, including the scoped empty-header-panel spacing fix. Footer
source destinations and generated HTML activate their links with hreflang="uk".
Preserve the eight XHTML files during upgrades and deploy them before the footer.
See ecosystem-pages.md for deployment and later bilingual content selection.

## Added operator article wrappers and fragments (2026-10-11)

Added src/main/webapp/contacts.xhtml and about-dntb.xhtml, with corresponding
uard/content/*.uk.xhtml fragments. They use the native template and existing
.uard-about CSS. User-supplied Ukrainian content appears in either UI locale.
Footer destinations and generated HTML activate both operator-column links with
hreflang="uk". No native header/footer template, Java, CSS, JavaScript or locale
settings were changed. Preserve these four files during upgrades; see operator-pages.md.

## Added Policies overview wrapper and fragment (2026-10-11)

Added src/main/webapp/policies.xhtml and uard/content/policies.uk.xhtml. They reuse
the native template and Ukrainian-only editorial fallback. Nested ordered lists
preserve the supplied main/child hierarchy; a dedicated .uard-about__policy-list
CSS counter rule displays hierarchical numbers. Footer sources/generated HTML
activate the policy destination with hreflang="uk". No existing native JSF template,
Java, JavaScript, authentication or locale logic was changed. Preserve both XHTML
files and the scoped stylesheet rules during upgrades; see policies-page.md.

## Publication email locale propagation 2026-10-22

Capture the native JSF locale in `DataverseRequestServiceBean` and carry an optional
immutable locale in `engine/command/DataverseRequest`. Persist it for waiting
workflows through `workflow/PendingWorkflowInvocation`'s existing map, excluding
the reserved metadata key from resumed step data. No schema migration is added.

`engine/command/impl/FinalizeDatasetPublicationCommand` passes that locale to
publication success/failure and associated granted-access notifications through
new `UserNotificationServiceBean` overloads. `MailServiceBean`, `util/MailUtil`,
and `branding/BrandingUtil` use one selected bundle for email subject, body,
greeting, closing and support label. Existing callers without an explicit locale
retain the prior fallback behavior. No global locale, recipient-language profile,
authentication, permission, translation-file, or SMTP configuration change is made.

Upgrade review: preserve request capture, the pending-workflow reserved map key,
and explicit bundle propagation together. Recheck upstream recipient-language
support before carrying this patch to another version. See
[publication-email-language.md](publication-email-language.md) for behavior,
tests, runtime verification, deployment and pending-workflow rollback notes.

## ORCID login presentation (2026-10-29)

`src/main/webapp/loginpage.xhtml` selects `org.login.orcid.blurb` only for the
existing ORCID ID condition and removes its two long ORCID explanation paragraphs.
The native heading/button/logo, OAuth `linkFor(providerId, redirectPage)` binding,
support component/parameters, forms, rendering and redirect metadata are preserved.
No Java, credentials, provider configuration, settings or database data changes.

The new grouped `org.*` key is in the packaged English
`src/main/java/propertyFiles/Bundle.properties` and both existing external bundles
under `customization/lang/`. The Ukrainian external bundle also corrects the
single ASCII apostrophe in `auth.providers.blurb` to doubled apostrophes for
`MessageFormat`, fixing literal `{3}` output. Preserve the pre-existing Ukrainian
bundle replacement and all other translations; merge only these entries remotely.

Upgrade review: compare upstream login/ORCID presentation and carry this small
view diff and bundle key together. Retain the four format parameters (support at
index 3), enabled-provider selection, native locale and OAuth redirect flow.
Local compile/FRD, HTTP builtin login smoke, XML, Java MessageFormat/Jakarta EL,
binding/preservation checks and branding consistency pass. Local ORCID is not
configured; authorization/callback and ORCID-only layout require test-server QA.
See [orcid-login.md](orcid-login.md) for the exact file map, incremental bundle
merge, verification limits, deployment and acceptance steps. The feature commit
excludes the pre-existing wholesale Ukrainian translation replacement; the full
local/upload bundle retains it. No remote deployment was performed.

## Ukrainian collection terminology and My Data tooltip (2026-11-01)

The external Ukrainian bundle uses колекція for the content container, підколекція
for nested collections, and батьківська колекція for Host Dataverse. Action labels
retain infinitive verbs. Account wording uses обліковий запис репозитарію where
reviewed; Dataverse platform attribution, technical identifiers and URLs remain.

Three upstream presentation files connect the My Data card tooltip to the existing
native bundle.dataverse key: mydata_fragment.xhtml supplies an escaped data
attribute on the existing result container; resources/js/mydata.js passes its
value into the Nunjucks render context; mydata_templates/cards_minimum.html uses
that value for the icon title. Nunjucks autoescaping remains enabled. Search,
permission checks, object types, routes, actions and locale selection are unchanged.
No Java or database changes are required. During upgrades, carry these three small
diffs together or use an equivalent upstream localization hook. See
[ukrainian-collection-terminology.md](ukrainian-collection-terminology.md) for the
translation manifest, remote merge procedure and verification.

## Local Ukrainian User Guide route and resources (2026-11-01)

One url-mapping in src/main/webapp/WEB-INF/pretty-config.xml forwards /guide/ to
the added static guide/index.html. Existing mappings are retained. The native
header is unchanged: :NavbarGuidesUrl supplies the destination and retains its
new-tab/noopener behavior, translated labels and session locale. The local hook
uses the canonical trailing slash. The first-visit link fix below supersedes
this section's original unchanged-header behavior.

Added src/main/webapp/guide/ is generated by scripts/customization/build-guide.py
from customization/guide/. Each chapter is a complete page with normal chapter
and heading links, so its URL is shareable and supports direct entry, refresh,
Back/Forward and navigation without JavaScript. The sidebar and page title match
the current chapter. The existing Bootstrap stylesheet is reused; no guide
JavaScript runtime is required.

The supplied Ukrainian editorial text and explicit Dataverse 6.6 attribution
are preserved. Four image elements use supplied translated JPGs under guide/pics,
with unchanged source image bytes, Ukrainian alt text and responsive dimensions.
The builder validates and copies referenced local images.

Upgrade review: preserve the mapping, build step and generated pages/images
together; do not replace the real header or alter authentication/permissions.
The hook is a database setting configured per environment. No uard2 change is
included. See [local-user-guide.md](local-user-guide.md).

## Preserve the Dataverse logo wordmark (2026-11-01)

`src/main/webapp/dataverse_header.xhtml` uses the literal product name Dataverse
next to the existing orange icon in the default navbar logo. The generic
`bundle.dataverse` remains localized as Колекція in Ukrainian for collection
labels and tooltips. The custom-logo rendering conditions, homepage link, icon,
locale selector, authentication, permissions and native JSF bindings are retained.

Upgrade review: carry this one-line presentation change unless upstream offers
a separate product-brand label. Merge the same line into the remote active
`dataverse_header.xhtml` and reload through the normal application workflow;
no language bundle or database setting change is needed. See the logo exception
in [ukrainian-collection-terminology.md](ukrainian-collection-terminology.md).

Verified locally: required fast redeploy succeeded; browser checks show Dataverse
in Ukrainian and through native Ukrainian -> English -> Ukrainian switching.
XHTML parsing and the exact one-line source diff pass; the Ukrainian collection
key remains Колекція. No remote deployment is included.

## About and User Guide first-visit links (2026-11-05)

`src/main/webapp/dataverse_header.xhtml` uses ordinary `<a href="#{...}">`
anchors for About and User Guide instead of `h:outputLink`. This avoids JSF URL
session encoding for these informational destinations. On the public server,
both `/about;jsessionid=...` and `/guide;jsessionid=...` return 404; adding a
trailing slash before the session parameter also returns 404. The issue occurs
on both the homepage and collection pages when opened without a session cookie.

The existing settings expressions, official-guide fallback, About rendering
condition, translated labels and `noopener` are preserved. About has no `target`
attribute and opens in the current tab; User Guide retains `target="_blank"`.
Authentication, permissions, locale actions, other links and global session
tracking remain unchanged. No database setting or proxy change is required.

Upgrade review: preserve these two anchor substitutions unless upstream provides
equivalent control over URL session encoding. Deploy the header through the
normal application workflow. Verify fresh-cookie and returning sessions on both
the homepage and a collection page, and follow both links. Canonical directory
destinations should still include the trailing slash in each environment's settings.

Verified locally: XHTML parsing, whitespace checks and required fast redeploy pass.
Fresh and returning sessions on `/` and `/dataverse.xhtml` receive clean menu URLs;
both destinations return HTTP 200. Native login URL session encoding is retained.

## Aggregate Make Data Count metrics (2026-11-08)

`src/main/java/edu/harvard/iq/dataverse/metrics/MetricsServiceBean.java` compares
the first seven characters of `datasetmetrics.monthyear` with the requested
`YYYY-MM` for cumulative MDC totals. This includes the entire requested month
for both older `YYYY-MM` rows and imported `YYYY-MM-DD` dates, excluding later
months, consistently with the existing monthly grouping. Stored reports and
dataset-level metrics are unchanged.

The shared subtree helper now converts native numeric IDs to `Long` and formats
them without narrowing to `int`. All its callers still select the same released
Dataverse/Dataset descendants; Dataverse queries retain the released parent,
and empty subtrees retain the SQL `IN (null)` behavior. An empty MDC time series
now returns `[]` rather than attempting to read a nonexistent first row.

`src/main/java/edu/harvard/iq/dataverse/api/Metrics.java` uses the versioned
`MDC-toMonth-v2-` cache prefix only for cumulative MDC totals (both the current
month shortcut and explicit `toMonth`). Existing cumulative entries can contain
incorrect zeros indefinitely for historical months. They remain stored but are
no longer read by these endpoints. Monthly MDC and all other metric cache keys,
country/collection scoping, and expiration rules are unchanged. No global cache
clear, database migration or report reimport is required.

Regression coverage: `MetricsTest` checks stale-cache bypass and reuse of the
corrected cache for current/historical months, with and without country/parent
filters, and retention of monthly MDC cache keys. `MetricsServiceBeanIT` executes
the service SQL against isolated PostgreSQL fixtures, covering month/year
boundaries, both date formats, nested and empty collections, country filters,
published-only descendants and IDs above `Integer.MAX_VALUE`. Run with:

```bash
mvn -Dtest=MetricsTest,MetricsUtilTest,DatasetMetricsServiceBeanTest -DskipIntegrationTests test
mvn -Dit.test=MetricsServiceBeanIT -Dpostgresql.server.version=17 failsafe:integration-test failsafe:verify
```

Verified locally on 2026-11-08: all 30 focused unit tests and 8 PostgreSQL 17
integration cases pass, with no skipped cases. FRD succeeded on Dataverse 6.12.
The existing pilot dataset `doi:10.5072/FK2/FW4KVR`, aggregate current/November
totals, monthly series and `parentAlias=uard` variants all report 1; October
reports 0. The nested `adminntest` collection reports 1; empty `dntb` returns 0
and `[]`. Country filtering and repeated cache reads pass. The original
`datasetmetrics` row and all preexisting MDC cache rows are unchanged. Shared
helper smoke checks for dataverse/dataset totals also return HTTP 200.

Upgrade/deployment: retain both Java changes together until upstream supplies
equivalent fixes. Use the ordinary application redeploy workflow; Git publication
and deployment to uard2 remain separate actions.

## ORCID login link to the local User Guide (2026-11-01)

The `org.login.orcid.blurb` entry in the packaged English fallback and the
external English/Ukrainian bundles restores a learn-more link to `/guide/`.
The root-relative destination uses the current installation, including uard2,
and opens a new tab with noopener. The native support parameter `{3}` and login
view bindings remain unchanged. No Java or XHTML change is required.

Upgrade/deployment: retain the updated entry in all active bundles and reload
through the existing Payara process to clear translation caches. Merge only the
one key remotely; do not replace maintained server translations. See the latest
section of [orcid-login.md](orcid-login.md) for exact entries and acceptance.

## Migrated environment FRD recovery helpers (2026-11-01 publication)

Previously local fixes in scripts/dev/dev-start-frd.sh and dev-frd.sh are now
maintained with the shared dev-deploy-frd.sh and isolated regression tests.
After full build/start, only the application container is refreshed to see the
new exploded directory. Readiness is bounded. Quick FRD keeps containers running;
both wrappers verify bind mounts and propagate deployment failures through the
shared helper, which cleans its temporary development password file.

These are development workflow changes, not production deployment scripts or
database recovery commands. Upgrades should preserve failure propagation and
recheck how upstream handles exploded WAR mounts. The compose override enables
the mounted native language directory through JAVA_TOOL_OPTIONS. See
[FRD troubleshooting](frd-troubleshooting.md) and [local setup](../DEV_SETUP.md).

## Ukrainian generated support name and title (2026-11-02)

src/main/java/edu/harvard/iq/dataverse/branding/BrandingUtil.java supports optional
org.support.team.root and org.contact.header.root bundle entries for generated
support names. Ukrainian uses Підтримка {0} as a label and Зв’язатися з підтримкою
{0} as the contact-dialog title, with the native root collection name as argument.
Explicit support-email personal names retain precedence; locales without the new
keys retain the old behavior. A bundle-accepting contact-header overload follows
the existing support-name pattern and allows isolated localized regression tests.

Keep the helper and Ukrainian entries together during upgrades/deployment.
No native contact-form XHTML, mail transport/recipients, permissions or locale
selection is changed. The generated support name is reused in native links/email
branding. See support-form.md. Remote deployment was not performed.

## Native contact Turnstile integration (2026-11-02, local test mode)

SendFeedbackDialog and contactFormFragment.xhtml select configured arithmetic or
Turnstile protection while retaining native form fields, recipients/CC and mail
composition. SendFeedbackDialog gates the mail action on server verification and
marks JSF validation failed on rejection/unavailability. Arithmetic receives an
action-level check as well. No authentication or SMTP logic/settings are changed.

JvmSettings adds contact CAPTCHA properties, defaulting to math. Added feedback/
ContactCaptchaService and TurnstileVerifier validate configuration and call the
fixed Cloudflare HTTPS endpoint with bounded timeouts. Real mode checks hostname
and contact_form action; official dummy credentials/responses require explicit
loopback-only test mode. Missing/bad configuration fails closed without fallback.

Added resources/js/uard/contact-turnstile.js manages widget lifecycle across native
AJAX updates, retry, expiry, close/reopen and responsive sizing. Dataverse's existing
Dialog.postShow override omits the standard onShow hook, so the new code observes
visibility/content changes without modifying native resize code. The view loads
the script with a content-version query. Preserve the script/view/version together.

Keep all service, configuration, view, JavaScript, external CSS and message changes
together during upgrades. Existing native locale maps ua to widget uk. Focused
tests mock mail delivery; no real support message was sent. See turnstile-local.md
for current local dummy configuration, checks, deployment map and rollback.

The contact view initially hides the verification retry control and makes our
duplicate status screen-reader-only. The widget script reveals both only on
error; regular pending/success stays compact while preserving aria-live updates.
The matching stylesheet tightens the verification panel. Vendor widget contents
and required branding are not modified.

## Native API multipart filenames

The single-file add and replace endpoints in `Datasets.java` and `Files.java`
recover UTF-8 filenames from Jersey's multipart header before persisting them.
`FileUtil.decodeFileName` keeps the supplied name when strict decoding fails,
including names sent in legacy Latin-1 that are invalid UTF-8. This correction
applies to the multipart filename only. File names supplied in JSON for direct
storage uploads bypass it.

See [Dataverse issue #12470](https://github.com/IQSS/dataverse/issues/12470).
On upgrade, check whether Jersey or Dataverse already decodes the multipart
filename. Verify Cyrillic and ASCII names via both add and replace before
keeping or removing the workaround.

## UTF-8 BOM in CSV and TSV ingest

`CSVFileReader.read` skips a leading UTF-8 byte order mark before parsing
the first header field. This keeps the mark out of variable metadata and the
downloaded tabular header, including when the first field is quoted. A
`U+FEFF` character inside a value is preserved. This applies when ingesting
a file. Previously ingested files retain their stored metadata.

On upgrade, check upstream BOM handling with quoted and unquoted first
headers. `CSVFileReaderTest` covers both cases and checks the generated
tabular header and value.
