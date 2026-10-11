# I18N_AND_CONTENT.md

## Two different concerns

### 1. UI internationalization
Examples:
- Search
- Add Dataset
- Log In
- Help
- Latest datasets
- Featured collections

These should be treated as UI strings.

For Dataverse XHTML:
- use Dataverse/JSF bundle keys;
- avoid hardcoded language-specific text.

For React:
```text
src/locales/
  en.json
  uk.json
```

### 2. Editorial/content translation
Examples:
- welcome text
- institutional description
- article text
- project stories
- news
- long homepage sections

Do not treat long editorial text as ordinary UI keys.

Possible structure:
```text
content/
  home.en.json
  home.uk.json
```

or:
```text
content/
  en/
    home.json
    about.json
  uk/
    home.json
    about.json
```

If non-developers need frequent editing, consider a CMS/content API later.

## Dataverse Ukrainian support
Current language mount:
```text
customization/lang/
```

At minimum:
```text
Bundle.properties
Bundle_ua.properties
```

The current Ukrainian bundle originated from the community language-pack repository and is older than Dataverse 6.12. It should be reviewed and completed progressively.

Configured JVM property:
```text
-Ddataverse.lang.directory=/var/www/dataverse/lang
```

## Locale source of truth
The Dataverse locale/language selector should remain authoritative.

React homepage:
- read/receive the current Dataverse locale;
- do not maintain an unrelated second language preference;
- update both UI labels and content selection when Dataverse language changes.

## Custom key namespace
For source-level Dataverse bundle additions, use a clear prefix, for example:

```properties
org.header.home=Home
org.header.about=About
org.footer.contact=Contact
```

Ukrainian:
```properties
org.header.home=Головна
org.header.about=Про проєкт
org.footer.contact=Контакти
```

Keep custom keys grouped and documented for easier upgrades.

## UaRD language decision and source layout

The user confirmed Ukrainian for first-time visitors. The running installation
currently uses legacy locale key `ua` in `:Languages` and `Bundle_ua.properties`.
The JSF default in `WEB-INF/faces-config.xml` is therefore `ua`; explicit language
changes still go through DataverseSession. Browser language preference does not
override the initial Ukrainian default. A future migration to standard `uk`
must coordinate all server-side settings/bundles; the hero already normalizes
either key to `uk`.

Current homepage sources:
- UI: `customization/homepage/locales/{en,uk}.json`.
- Editorial: `customization/homepage/content/home.{en,uk}.json`.
- Build: `python3 scripts/customization/build-homepage.py`.

The generated no-JavaScript fallback is Ukrainian. No navigator.language,
localStorage or independent homepage language toggle is used. The official logo
remains UaRD in both languages. The English institutional name is provisional
until confirmed. Preserve room for 30–40% text expansion.

Use separate curated mappings for translated institution/knowledge-area/cluster
labels. Do not silently translate stored dataset titles or authors. Date/status
formatting can be localized without rewriting stored metadata.

## Bilingual informational pages (2026-10-11)

Use one public XHTML route per article and dataverse_template.xhtml for its
shared shell. Select longer editorial Facelets fragments server-side using
DataverseSession.localeCode: the current site's en selects English, and ua/uk
select Ukrainian. Use .en.xhtml/.uk.xhtml content filenames and lang="en"/
lang="uk" on the articles without renaming working server locale settings.
The current fallback is Ukrainian; explicitly extend the mapping when adding
other supported languages. Localize the page title and description metadata too.

Keep native language switching authoritative. Do not add a browser-language
preference, localStorage, a custom selector or a frontend-only content swap.
Reuse org.* bundle keys for common UI strings; keep long editorial translations
outside the native UI bundles. Preserve native header/footer/authentication and
support through the existing JSF template.

See [the About-page development guide](docs/custom-about.md#one-url-two-article-languages)
for the wrapper, fragment layout, footer changes, deployment and acceptance.
About now implements this pattern using the supplied Ukrainian and English articles.
Use it for future informational pages once their translations are supplied/reviewed.

The short top-menu system article is a separate /system.xhtml page with a
system.uk.xhtml fragment. Until English editorial content is supplied, it renders
Ukrainian in both native interface languages with lang="uk". Keep the native
header/footer language selection intact. Add an English fragment and localized
metadata only when content is ready; see [the system-page guide](docs/custom-system.md).

The four EOSC/OpenAIRE/re3data/FAIR footer articles also use separate *.uk.xhtml
editorial fragments and display the supplied Ukrainian text in either UI locale.
Their footer links explicitly identify Ukrainian content with hreflang="uk".
When English copy is supplied, follow the About-page wrapper/metadata pattern
and remove the fixed destination language declaration. See
[the ecosystem-page guide](docs/ecosystem-pages.md).

The operator Contacts and About DNTB pages use the same Ukrainian-only editorial
fragment fallback in both native UI languages, with uk article/footer destination
language tags. When English content is supplied, follow the shared About wrapper
pattern and localize metadata too. See [operator-pages.md](docs/operator-pages.md).

The Policies overview also uses a Ukrainian editorial fragment in either UI locale.
Its English parenthetical policy names are part of the supplied Ukrainian article,
not a complete English translation. Follow the shared wrapper pattern when full
English copy is available; see [policies-page.md](docs/policies-page.md).
