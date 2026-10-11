# CUSTOMIZATION_STRATEGY.md

## Principle
Keep supported customization separate from upstream source modification.

## Layer 1 — Dataverse-supported branding/configuration
Use for:
- custom homepage wrapper/bootstrap
- external CSS
- logos
- basic header/footer additions
- settings-based branding

Current files:
```text
customization/branding/
  custom-homepage.html
  custom-header.html
  custom-footer.html
  custom-stylesheet.css
```

Important:
`HeaderCustomizationFile` adds content; it does not replace the actual Dataverse navbar.

## Layer 2 — XHTML/JSF source customization
Use when the actual application structure must change.

Primary file:
```text
src/main/webapp/dataverse_header.xhtml
```

Possible footer source file should be identified before modifying.

### Rules when editing XHTML
Preserve:
- `rendered` conditions
- permission checks
- authentication state
- action bindings
- PrimeFaces/JSF behavior
- user menu logic
- notifications
- locale selector
- search form behavior
- Add Data permissions

Prefer changing:
- wrappers
- DOM grouping
- classes
- layout
- visual order

before changing application logic.

## Layer 3 — React homepage
Recommended if the homepage becomes complex.

Suggested architecture:
```text
Dataverse JSF shell
├── real Dataverse header/footer
└── custom homepage mounting a React app
```

Possible React responsibilities:
- hero
- search
- featured collections
- latest datasets
- metrics
- cards
- editorial blocks
- article previews
- API-driven widgets
- multilingual presentation

Keep React independent enough that it can be built/deployed without rewriting the whole Dataverse frontend.

## API use
Prefer Dataverse REST APIs for dynamic homepage data rather than scraping rendered JSF pages.

## Upgrade strategy
Every upstream source file changed becomes an upgrade review point. Record all modified upstream files in:

```text
docs/upstream-modifications.md
```

## Current UaRD implementation decisions

The controlling product assessment is `docs/uard-requirements.md`; execution
order is `docs/customization-plan.md`. The provided screenshot establishes
structure, while the specification asks us to improve its brand hierarchy,
consistency, navigation and accessibility.

Current hero: plain HTML/JavaScript generated from `customization/homepage/`
into the supported branding hook, with scoped CSS and external versioned assets
under application resources. See `docs/resource-assets.md`. React
remains an option for future complexity, not a dependency of the hero.
Advanced search and results use native Dataverse pages.

The only current intentional upstream source change is the JSF default-locale
configuration, requested by the user. Navbar restructuring is still planned.
A future shared design system must cover internal collection/search/dataset
templates as well as the homepage. Organizations, knowledge areas and clusters
have separate content models and must not be collapsed into one generic carousel.
