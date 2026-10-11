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

The generated UaRD homepage script now loads institution logos through the
existing `/api/access/dvCardImage/{id}` endpoint, with a generic-icon fallback.
This reuses native theme/logo handling without changes to Java or JSF templates.
See resource-assets.md for behavior and verification.
