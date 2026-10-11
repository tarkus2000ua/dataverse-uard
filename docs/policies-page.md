# UaRD Policies overview (2026-10-11)

Public route: /policies.xhtml, linked by the existing footer policies entry.
Sources: src/main/webapp/policies.xhtml and uard/content/policies.uk.xhtml.
The wrapper uses dataverse_template.xhtml and the article reuses .uard-about styles.

The user-supplied introduction explicitly states that the policies will be
published after approval of the repository regulations and operating procedure.
The supplied titles are presented as a planned policy overview, not links to
adopted policy documents. Text, English parenthetical names and the R08 annotation
are preserved. The page contains nine main items and fourteen nested items in
three groups: 7.1-7.7, 8.1-8.4 and 9.1-9.3.

## List structure and localization

The markup uses semantic nested ol/li elements. A scoped .uard-about__policy-list
rule uses CSS counters in ::marker to render hierarchical numbers without
JavaScript. All other article lists and compact muted footer rows are unchanged.
Group labels 7-9 use the same regular weight as the other items, as requested.
The existing article font/spacing and marker color are retained. The article is explicitly lang="uk", and its footer link carries
hreflang="uk". Both native interface languages display Ukrainian content until
English text is supplied. Native session language selection remains authoritative.

To add English later, follow custom-about.md: add policies.en.xhtml, choose the
fragment using DataverseSession.localeCode, localize title/description and remove
the fixed Ukrainian footer destination language. Preserve editorial text in
fragments rather than native UI bundles.

## Build and deployment

Generate footer HTML using python3 scripts/customization/build-footer.py and check
with --check. Do not edit the generated branding fragment directly. Local XHTML
changes require ./scripts/dev/dev-frd.sh; mounted stylesheet/HTML refresh directly.

For direct Payara with the existing UaRD baseline, deploy in this order:

1. uard/content/policies.uk.xhtml inside the active application.
2. Updated custom-stylesheet.css to the existing configured branding style path,
   preserving any remote additions when merging an older stylesheet baseline.
3. policies.xhtml beside dataverse_template.xhtml and WEB-INF in the active app.
4. Check /policies.xhtml, then publish generated custom-footer.html to its configured path.

The application user must be able to read the files. Keep wrapper/fragment in the
maintained deployment artifact for later redeployments. Packaged installations
need them in the normal rebuilt WAR. Check paths/permissions and reload/redeploy
the application if copied views are not discovered; a full Payara restart is not
automatically required. CSS-only replacement does not require an application restart.
No Java, JavaScript, native translation bundle or API setting change is needed.

The combined footer-article package now includes seven Ukrainian pages (14 XHTML
files), current stylesheet and generated footer, covering the current ecosystem,
operator and policy destinations together. The existing bilingual About and
short top-menu System pages remain separate. Remote deployment is not performed.

## Verification

Both XHTML files parse as XML. The nine-item hierarchy and all fourteen child
entries are checked against the supplied text. Local FRD and both branding builders --check succeeded. Actual footer navigation
and return-home worked. Native UA -> EN -> UA kept the supplied Ukrainian content
and metadata, with its translated footer label. Main groups 7-9 have regular 400
font weight. Nested numbering 7.1-9.3 was visually verified; the semantic list
contains nine root items and fourteen children. Layouts at 1440/768/375/320px had
no overflow or header gap, and the compact muted footer styling remained intact.
The article/list and footer link also render without JavaScript. No JavaScript
page errors occurred. XML, text hierarchy, git diff --check and preservation of
unrelated files/index passed.
