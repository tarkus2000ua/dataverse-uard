# Upstream modifications

This file records intentional changes to Dataverse 6.12 source and their upgrade
review points. Branding HTML/CSS, translations and assets under `customization/`
are maintained separately.

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
