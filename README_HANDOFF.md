# UaRD - continuation handoff

Updated 2026-11-01. Target: Dataverse 6.12. This file describes the current
implementation and supersedes earlier dated checkpoints. It does not select a
new task or authorize a remote application deployment.

## Repository and required reading

Repository: /home/andrey/projects/dataverse in WSL Ubuntu.
Windows: \\wsl.localhost\Ubuntu\home\andrey\projects\dataverse.
Keep the repository in the WSL Linux filesystem and use WSL Git/build tools.
Do not copy/sync the checkout into a Windows repository.

Read AGENTS.md, CODEX_CONTEXT.md, DEV_SETUP.md, CUSTOMIZATION_STRATEGY.md,
GIT_STRATEGY.md, I18N_AND_CONTENT.md, and these feature references:
- docs/upstream-modifications.md
- docs/resource-assets.md
- docs/homepage-navigation.md
- docs/homepage-collections.md
- docs/homepage-clusters.md
- docs/homepage-knowledge-areas.md
- docs/new-pc-setup.md
- docs/frd-troubleshooting.md
- docs/custom-footer.md
- docs/custom-about.md
- docs/custom-system.md
- docs/ecosystem-pages.md
- docs/operator-pages.md
- docs/policies-page.md
- docs/publication-email-language.md
- docs/orcid-login.md
- docs/ukrainian-collection-terminology.md
- docs/local-user-guide.md
- docs/remote-deployment.md (newest incremental sections supersede baseline notes)

Product/design context: docs/uard-requirements.md and docs/customization-plan.md.
Treat older implementation notes as history; inspect the actual source/runtime.

## Git state and preservation

Current feature branch: codex/login-local-guide.
Origin: git@github.com:tarkus2000ua/dataverse-uard.git.
Upstream: https://github.com/IQSS/dataverse.git.

This branch includes the preceding feature commits:
- e39d0c13b1 - Keep Dataverse product name in navbar logo.
- 2e7f9d430a - Add local Ukrainian guide with direct chapter URLs.
- 891a82c41b - Use collection terminology in Ukrainian UI.
- db72d6afe8 - Customize ORCID login page for UaRD.

The 2026-11-01 publication also includes the restored login-to-guide link,
the installed Ukrainian translation baseline, migrated-environment FRD helpers,
and updated project documentation. Inspect git log/status and origin tracking
for exact final revisions and publication status. The user explicitly requested
committing the necessary project changes and pushing this feature branch;
that request does not authorize remote deployment or future unrelated pushes.

Preserve work intentionally excluded from publication:
- Staged customization/branding/custom-header.html: unused local placeholder.
  The local :HeaderCustomizationFile setting is absent; do not activate it.
- Untracked customization/homepage/assets/: original design materials.
- Untracked dataverse.zip: local source/archive input, not a deployment artifact.
- Any additional staged, unstaged or untracked user work found by git status.

Use explicit path selection or a separate Git index so unrelated staged work
never enters a commit accidentally. Do not reset or discard it.

## Verified local environment

Windows 11, WSL2 Ubuntu, Docker Desktop, Java 21 and Maven.
Local application: http://localhost:8080; version API reports 6.12.
Existing running containers: dev_dataverse (gdcc/dataverse:unstable),
dev_postgres (postgres:17), dev_solr (solr:9.8.0).
The exploded application is target/dataverse, mounted at
/opt/payara/deployments/dataverse. Branding and language mounts are
/var/www/dataverse/branding and /var/www/dataverse/lang.

The compose override supplies dataverse.lang.directory through JAVA_TOOL_OPTIONS.
Keep it when merging other JVM options. Existing data and Payara configuration
come from the migrated environment; Git does not contain the database, uploaded
logos or dataset files. Compose still reports the pre-existing unset STORAGE_DIR
and obsolete version warnings; they were not changed in this publication.

Do not reset, rebuild or recreate Docker volumes/databases without explicit
approval. Do not run startup/seed commands to test documentation. See
frd-troubleshooting.md before addressing stale bind mounts.

## Current UI and localization

- Custom bilingual homepage and footer use generated branding fragments and the
  real native Dataverse shell. The hero's attribution is now a repository button.
- Native navbar authentication, notifications, permissions, search, Add Data,
  support and locale switching remain JSF-backed.
- The default orange-icon logo displays the product name Dataverse in every
  locale. Only its wordmark uses a literal product name; collection labels still
  use the localized bundle.dataverse value, Колекція in Ukrainian.
- Reviewed Ukrainian container terminology uses колекція, підколекція and
  батьківська колекція. The 275-entry manifest and nontechnical report are in
  customization/translations and docs/reports. Action labels remain infinitives:
  Додати, Видалити, Редагувати, Опублікувати, Пов’язати.
- My Data collection tooltips use the native translated label. Keep its XHTML,
  JavaScript and Nunjucks presentation changes together.
- The active Ukrainian baseline was supplied from the remote installation and
  installed locally on 2026-10-22. Its current 3027 entries include later reviewed
  changes; this is not a claim that the entire bundle was reviewed for 6.12.
- Native locale remains authoritative: local English en and Ukrainian legacy ua.
  Keep the server's working ua/uk code and bundle filename during deployment.
- ORCID login retains the concise UaRD explanation, native support command and
  sign-in button. org.login.orcid.blurb now includes a new-tab /guide/ learn-more
  link in English and Ukrainian, with rel=noopener and the existing {3} support
  parameter. Other OAuth, builtin and Shibboleth behavior is retained.
- Publication email locale propagation is documented separately; do not change
  SMTP, authentication or workflow settings for UI translation work.

## Informational pages

All wrappers below use dataverse_template.xhtml and editorial fragments under
src/main/webapp/uard/content. Longer editorial content stays separate from UI keys.

| Public route | Link location | Content |
| --- | --- | --- |
| /about.xhtml | Footer | about.uk.xhtml and about.en.xhtml |
| /system.xhtml | Native top-menu About | system.uk.xhtml |
| /eosc.xhtml | Footer | eosc.uk.xhtml |
| /openaire.xhtml | Footer | openaire.uk.xhtml |
| /re3data.xhtml | Footer | re3data.uk.xhtml |
| /fair.xhtml | Footer | fair.uk.xhtml |
| /contacts.xhtml | Footer | contacts.uk.xhtml |
| /about-dntb.xhtml | Footer | about-dntb.uk.xhtml |
| /policies.xhtml | Footer | policies.uk.xhtml |

About selects English for native locale en and Ukrainian for ua/uk/fallback,
including metadata. The other supplied articles remain Ukrainian in either UI
locale, with lang/hreflang=uk. Policies keeps supplied pending-approval wording,
9 main items and 14 nested items; groups 7-9 use regular weight.

The native Featured Dataverses API supplies institution cards and their original
logos, with native image fallbacks. Theme IDs are not collection IDs; use the
native logoOwnerId and theme.logo resolution, preserving aspect ratio and
fallbacks. Do not hardcode stored IDs or logo filenames.
Knowledge areas A-K and fifteen clusters use
canonical English facet values under the remote /dataverse/uard alias; local
metadata/root differences remain a separate QA consideration.

## Ukrainian User Guide

Editable template/fragments/images: customization/guide/.
Builder: scripts/customization/build-guide.py.
Public generated output: src/main/webapp/guide/.

Deploy generated output, never the editable customization/guide/index.html:
the source template contains {{guideContent}} and {{guideTitle}} placeholders.
Each generated chapter is a complete static page with sidebar/title/footer.

Routes: /guide/ and /guide/{account,find-use-data,dataverse-management,
dataset-management,tabular-data-ingest,appendix}.html. index.html and welcome.html
also work as complete introductions. Existing heading IDs provide shareable
section anchors. Normal links support reload, Back/Forward, new tabs and no-JS use.

One PrettyFaces mapping forwards /guide/ to /guide/index.html. Keep all existing
mappings. Four supplied translated JPGs are copied into guide/pics/ unchanged;
other supplied illustrations retain their external documentation URLs.
Styling reuses native Bootstrap. The supplied text explicitly cites Dataverse
6.6; this work did not review/rewrite its instructions for every 6.12 feature.

Mixed native .xhtml routes, static .html chapters and the /guide/ entry are
intentional. Clean /about/ and /system/ aliases remain unimplemented.

## Local workflow and settings

For the existing running environment use ./scripts/dev/dev-frd.sh. It compiles,
syncs the exploded application and calls dev-deploy-frd.sh; it does not recreate
containers. The deployment helper verifies both sides of the bind mount, propagates
errors and cleans its temporary development password file.

dev-start-frd.sh is the full build/start workflow. It refreshes only the application
container after rebuilding the exploded directory and bounds readiness polling.
Do not run it as a routine check against the working migrated instance.

Mounted branding HTML/CSS updates on refresh. Changed resources/XHTML use FRD.
Build and validate generated content before deploying it:

```bash
python3 scripts/customization/build-guide.py
python3 scripts/customization/build-guide.py --check
python3 scripts/customization/build-homepage.py --check
python3 scripts/customization/build-footer.py --check
./scripts/dev/dev-frd.sh
```

For changed homepage/footer JS, follow the resource-first workflow in
resource-assets.md before publishing fragments that reference new asset versions.

Current local hooks:
- :HomePageCustomizationFile=/var/www/dataverse/branding/custom-homepage.html
- :FooterCustomizationFile=/var/www/dataverse/branding/custom-footer.html
- :StyleCustomizationFile=/var/www/dataverse/branding/custom-stylesheet.css
- :NavbarAboutUrl=http://localhost:8080/system.xhtml
- :NavbarGuidesUrl=http://localhost:8080/guide/

Keep the trailing slash in NavbarGuidesUrl for first-visit JSF session-ID URLs.
HeaderCustomizationFile and LogoCustomizationFile are absent locally.
Settings are stored in the database and do not travel with Git.

## Remote deployment and current limits

Production: https://uard.dntb.gov.ua/.
Current test deployment target: https://uard2.dntb.gov.ua/.
The user performs remote file updates; the agent has not deployed to either host.
Actual active Payara path, admin port, configured language filename and final
remote file state still require confirmation. A user screenshot showed the guide
source placeholder on uard2; correct generated files were supplied, but successful
replacement has not been independently verified.

On the target server, `asadmin list-applications --long` and
`asadmin get 'applications.application.*.location'` identify the deployment.
Use the enabled application, not a source checkout or an old deployment copy.

Incremental file map:
- Guide: generated guide/ directory plus the /guide/ PrettyFaces mapping;
  set :NavbarGuidesUrl=https://uard2.dntb.gov.ua/guide/ for the test server.
- Logo: one-line wordmark change in active dataverse_header.xhtml.
- Login guide link: merge org.login.orcid.blurb into active English and Ukrainian
  external bundles; reload through the normal Payara process to clear caches.
- Collection terminology: reviewed bundle entries and the three My Data files;
  see the manifest/merge helper and nontechnical table.

Full bundle replacement is allowed when the user chooses it, after backing up
and preserving remote-only translations. Use current customization/lang files;
rename the Ukrainian upload only to match the server's existing ua/uk filename.
Older ORCID ZIPs predate the restored documentation link. No login XHTML change
is needed for this last bundle-only update. Keep application patches/resources
in the maintained remote artifact so later deployments retain them.

Do not upload dev startup scripts, local credentials, design archives or a stale
local target WAR. No database import, schema/metadata update, Solr reset or volume
recreation is part of these presentation updates. See remote-deployment.md.

## Verification and open work

Completed local checks include FRD deployment, native UA -> EN -> UA logo checks,
complete guide pages and direct section URLs, navigation/refresh/Back/Forward,
no-JavaScript and 375px layouts, translated image bytes, XHTML parsing and builders.
The restored login text passes Java 21 MessageFormat/link checks with the native
support placeholder. Mounted bundles match updated sources; builtin login and
/guide/ return HTTP 200. Isolated FRD and translation-merge regression tests pass.

Local ORCID is unconfigured; its final rendered layout and OAuth callback still
need checking on an existing ORCID-enabled server. Full authenticated QA and
all native Ukrainian translations are not claimed complete.

Other open work: English editorial copy for Ukrainian-only articles and guide;
real documents/institutions/statistics footer destinations; remote taxonomy QA;
approved bilingual stored institution names. No next feature is selected.

For the next user request, inspect actual Git/runtime state, preserve unrelated
work, and implement only the requested scope. Do not infer remote deployment
success or permission to change data from a source-code push.
