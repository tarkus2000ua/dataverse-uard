# UaRD About page (2026-10-11)

The bilingual article uses one public /about.xhtml URL and the native Dataverse
session locale. It reuses dataverse_template.xhtml for authentication, navigation,
the language selector, support and the custom/native footer. showDataverseHeader=false
suppresses the collection banner, not the application navbar.

## Source files and editorial content

```text
src/main/webapp/
  about.xhtml
  uard/content/
    about.uk.xhtml
    about.en.xhtml
```

about.xhtml is the shared Facelets wrapper. The two fragments contain only their
article markup, including the title, five sections, nested FAIR principles and
return-home link. Both share section IDs and the .uard-about CSS classes. Only
one article renders at a time; no header/footer markup is duplicated.

The original user-supplied Ukrainian article markup is preserved in about.uk.xhtml.
The English article uses the user-supplied English text with corresponding section
headings, paragraph/list formatting and bold principle labels. Supplied editorial
wording, including acronyms and policy/standards statements, is preserved; this
integration is not an independent factual or certification review.

Styles remain in customization/branding/custom-stylesheet.css. The existing About
styles apply unchanged to both languages, and institution-logo styling is retained.
No Java or custom language-switching JavaScript was added. Page title and description
metadata are also selected on the server.

## One URL, two article languages

The implementation below is also the pattern for future informational pages:

```xhtml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html>
<!-- One public article URL; native Dataverse locale selects the editorial fragment. -->
<ui:composition xmlns="http://www.w3.org/1999/xhtml"
                xmlns:ui="http://java.sun.com/jsf/facelets"
                template="/dataverse_template.xhtml">
    <ui:param name="pageTitle"
              value="#{dataverseSession.localeCode eq 'en' ? 'About the repository — UaRD' : 'Про репозитарій — UaRD'}" />
    <ui:param name="showDataverseHeader" value="#{false}" />
    <ui:define name="meta_header">
        <meta name="description"
              content="#{dataverseSession.localeCode eq 'en' ? 'About the National Research Data Repository: purpose, objectives, operating principles and international standards.' : 'Про Національний репозитарій дослідницьких даних: мета, завдання, принципи роботи та міжнародні стандарти.'}" />
    </ui:define>
    <ui:define name="body">
        <ui:include src="#{dataverseSession.localeCode eq 'en'
                          ? '/uard/content/about.en.xhtml'
                          : '/uard/content/about.uk.xhtml'}" />
    </ui:define>
</ui:composition>
```

The site's en locale selects English; ua and uk select Ukrainian. Other codes
currently fall back to Ukrainian. If more languages are added, extend the mapping
explicitly. Do not rename working ua server settings/bundles just because the
editorial filename and article language tag use standard uk.

Each content fragment wraps its article in ui:composition, for example:

```xhtml
<ui:composition xmlns="http://www.w3.org/1999/xhtml"
                xmlns:ui="http://java.sun.com/jsf/facelets">
    <article class="uard-about" lang="en" aria-labelledby="uard-about-title">
        <!-- Complete English article, including its title and return-home link. -->
    </article>
</ui:composition>
```

The Ukrainian fragment uses lang="uk". Keep matching section IDs/classes, and
translate the full editorial article, title, metadata and return-home label.
Use the org.* bundle namespace for reusable UI strings; keep long article text
in editorial fragments (see ../I18N_AND_CONTENT.md).

The native selector updates DataverseSession and redirects to the current page.
It remains the only language preference. Do not add navigator.language, localStorage
or a separate page language selector. The footer About destination is the string
"/about.xhtml" in customization/footer/links.json, with no fixed hreflang attribute.
Its bilingual label still follows the existing footer script. For a genuinely
single-language destination, the builder still accepts a url/hreflang object.

## Build and deployment

Run python3 scripts/customization/build-footer.py after changing its destination,
and run the builder with --check to verify the generated HTML. Do not edit the
generated customization/branding/custom-footer.html directly.

Local XHTML changes require ./scripts/dev/dev-frd.sh. Branding CSS/HTML are mounted
and refresh directly. This bilingual update changes no CSS, JS resources, native
language bundles or API settings.

For a direct Payara installation with the preceding About/UaRD baseline installed,
deploy these application files together, preserving their paths:

```text
<active-deployed-app>/about.xhtml
<active-deployed-app>/uard/content/about.uk.xhtml
<active-deployed-app>/uard/content/about.en.xhtml
```

Copy both fragments before replacing the wrapper that references them. Then publish
the generated custom-footer.html to the configured branding directory. For the first
About-page installation, also publish the integrated custom-stylesheet.css; the
preceding hero update separately requires custom-homepage.html. All XHTML files belong
inside the active Dataverse application, not the external branding directory.

Ask the remote administrator to confirm the active application directory (beside
dataverse_template.xhtml and WEB-INF) and readability by its service user. A full
Payara restart is not automatically required for copied XHTML, but reload/redeploy
the application if it does not discover the views after checking paths/permissions.
Include the wrapper and fragments in the maintained deployment artifact so later
redeployments retain them. Packaged WAR installations require a rebuilt artifact.
Preserve the server's maintained localization, settings and application changes.
Remote deployment has not been performed by this task.

## Verification

Original Ukrainian article markup and all supplied English paragraphs/list entries
were checked for preservation. All three XHTML files parse as XML. Local FRD redeployment succeeded on Dataverse 6.12. Browser checks passed
UA -> EN -> UA with matching article text, title, description and article language.
English selection persisted through return-home and footer navigation. Both articles
rendered server-side with JavaScript disabled (English used an existing English
session; a fresh session used Ukrainian). Each language rendered one article, five
sections and 27 list items with one custom footer. Native support opened/closed,
and the return-home link received keyboard focus. No page overflow occurred at
1440/768/375/320px in either language, and no JavaScript page errors were observed.
English desktop/mobile screenshots were visually inspected. Both branding builders
passed --check, XHTML XML checks passed and git diff --check passed. Unrelated
pre-existing files and the Git index were unchanged.

For this and future articles, verify UA -> EN -> UA through the native selector:
article text, page title, description and article lang must match. Also check a fresh
Ukrainian session, persistence of English when leaving/returning, the single footer
URL, exactly one article/header/footer, support, keyboard focus, no overflow at
1440/768/375/320px, and article rendering without JavaScript. Native language switching
itself still relies on the standard Dataverse client scripts.
