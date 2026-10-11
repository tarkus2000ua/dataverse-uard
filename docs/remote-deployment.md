# Deploying the UaRD customization to a remote Dataverse 6.12 server

This guide deploys the current homepage, header styling, collection strips and
custom footer onto an **existing Dataverse 6.12 installation**. It does not install
a new Dataverse instance or migrate the local development database.

Baseline: commit `1c5b70e053` on `codex/homepage-hero` (2026-10-04).
Build from that revision or an explicitly reviewed later revision. Keep the WAR,
branding fragments and asset versions from the same build together.

## Target server and working assumptions

Target: **https://uard.dntb.gov.ua/**. Public read-only checks on 2026-10-04 found:

- [Version API](https://uard.dntb.gov.ua/api/info/version): Dataverse **6.12**,
  build **`master-9769107`**.
- [Homepage](https://uard.dntb.gov.ua/): native collection/browse page with an
  English/Ukrainian language selector. The anonymous response inspected had
  document locale `en`; the new UaRD homepage/footer were not present.
- [Root featured list](https://uard.dntb.gov.ua/api/dataverses/:root/featured):
  successful anonymous response with an empty list (`[]`).
- Existing root branding and Knowledge Area/Clusters Subject facets are visible.
  Retain the existing collections, metadata definitions and stored branding data.

The user reports localization is already configured and the installation is
probably **direct Payara, without Docker**. Use section 4A as the primary procedure,
subject to a server-console check. Public responses do not establish the Payara
version, JVM, filesystem paths, Ukrainian locale code or service configuration.

**For this server, deploy a freshly built WAR and three branding files. Keep its
existing translation files, language-directory option and `:Languages` setting.**
The first-visit locale in the WAR must match the server's Ukrainian locale code
before deployment. Do not infer that code from the visible language label.

## 1. What to deploy

| File or directory in this repository | Remote destination | Purpose |
| --- | --- | --- |
| `target/dataverse-6.12.war`, freshly built | Existing Payara application deployment | Contains all application resources and the Ukrainian initial-locale change |
| `customization/branding/custom-homepage.html` | `/var/www/dataverse/branding/custom-homepage.html` | Homepage fragment, including both languages |
| `customization/branding/custom-footer.html` | `/var/www/dataverse/branding/custom-footer.html` | Custom footer fragment, including both languages |
| `customization/branding/custom-stylesheet.css` | `/var/www/dataverse/branding/custom-stylesheet.css` | Homepage, native header and footer styling |

Do **not** deploy `customization/lang/Bundle.properties` or `Bundle_ua.properties`
to this already-localized server. Preserve its maintained native UI/metadata
translations and their existing directory. Our local Ukrainian bundle is older
and incomplete. Custom homepage/footer translations are already included in the
generated HTML and do not replace the server's native bundles.

The WAR includes these customization files automatically:

- `resources/images/uard/`: selected hero WebP, UaRD logo PNG, heading SVGs,
  A–K folder SVGs and 15 cluster SVGs.
- `resources/js/uard/homepage.js` and `resources/js/uard/footer.js`.
- `WEB-INF/faces-config.xml`: first-time sessions default to legacy locale `ua`.

Do not copy resources separately when deploying this WAR. The browser loads them
at `/resources/images/uard/...` and `/resources/js/uard/...`.

**Not required on the runtime server:** original logo PDF, unused hero variants,
screenshots, `customization/homepage/assets/`, JSON/template/JS source files,
build scripts, example-collection seed scripts, local Docker Compose files or
local development credentials. Keep editable sources in Git/build tooling for
future releases. Do not deploy or activate the dummy `custom-header.html`.
The header is styled through CSS; its native JSF template is unchanged.

## 2. Check the target and prepare rollback

Run these checks on the remote server using SSH. Examples below are Bash/Linux
commands. Replace all example host names and deployment paths before use.

```bash
LOCAL_URL='http://localhost:8080'
curl --fail-with-body "$LOCAL_URL/api/info/version"
java -version
/path/to/payara/bin/asadmin version
/path/to/payara/bin/asadmin list-applications
curl --fail-with-body "$LOCAL_URL/api/admin/settings/:Languages"
/path/to/payara/bin/asadmin list-jvm-options \
  | grep -F 'dataverse.lang.directory'
```

For Docker, run the Java/Payara commands **inside the application container**;
`localhost:8080` must refer to the application listener. A request through a
host-published port may not be recognized as localhost by the API policy.

Verify:

1. Target Dataverse is 6.12 and runs at the site root `/`. This customization uses
   absolute `/resources/`, `/api/` and navigation paths; a `/dataverse` context
   prefix requires code changes before deployment.
2. The target runtime supports this checkout. Its
   `modules/dataverse-parent/pom.xml` specifies **Java 21 and Payara 7.2026.8**.
   Compare the actual target, not just its Dataverse version label. If incompatible,
   resolve compatibility in staging before deploying this WAR; this guide does
   not perform a Payara upgrade.
3. Record the deployed application name and Payara domain/target. Use the existing
   application name when replacing it, so a second root application is not created.
4. Identify whether deployment is direct Payara or an existing container/image
   pipeline. Also identify the actual Payara process UID/GID.
5. Confirm the reverse proxy forwards `/resources/`, `/api/`, native JSF pages and
   `/dataverse/...` routes. No separate static-assets server is required.
6. Confirm the existing Ukrainian locale in `:Languages` and the corresponding
   bundle filename. Our current WAR uses `ua`. If the server uses `ua`, retain its
   configuration unchanged. If it uses `uk`, prepare a reviewed deployment revision
   whose `faces-config.xml` defaults to `uk`, then adjust the WAR locale check in
   section 3. Homepage/footer JavaScript already recognizes both. Do not rename
   the server's bundles or rewrite its working language setting to fit our build.
7. Identify any server-specific application source changes behind build
   `master-9769107`. Reconcile them with the customization before replacing the
   entire WAR; matching the 6.12 version label alone does not establish identical
   source. Existing metadata definitions/data do not need to be reimported.

The language-directory check shows only that one JVM option. If it prints nothing,
check the service/environment configuration through the site's normal procedure;
absence from this output alone is not a reason to overwrite working localization.

Before replacing anything, retain the previous WAR or immutable image digest,
branding files, language bundles, JVM options and deployment configuration. Take
and verify a database backup using the site's normal backup procedure. These UI
changes introduce no custom schema migration; the database backup is protection
for application replacement and settings changes, not a request to replace data.

Export existing settings into a **private server-side backup directory**:

```bash
umask 077
BACKUP_DIR="$HOME/uard-backup-$(date -u +%Y%m%dT%H%M%SZ)"
mkdir -p "$BACKUP_DIR"
curl --fail-with-body "$LOCAL_URL/api/admin/settings" \
  > "$BACKUP_DIR/settings-before.json"
/path/to/payara/bin/asadmin list-jvm-options \
  > "$BACKUP_DIR/jvm-options-before.txt"
```

The settings export can contain secrets. Keep it on the secured server; do not
commit it, include it in the release archive or share its contents. Note which
branding/language settings are absent as well as those that have values.

Admin API commands assume the server's configured access policy allows local
administration. With `localhost-only`, no user API token is needed for these
settings calls. With `unblock-key`, use the existing administrator-supplied
`X-Dataverse-unblock-key` header through the site's secure procedure. This key
is different from a user API token. With a `drop` policy, follow the site's
administration procedure. Do not expose admin endpoints publicly to make these
commands work. See [Dataverse 6.12 API access configuration](https://guides.dataverse.org/en/6.12/installation/config.html#blocking-api-endpoints).

## 3. Build one release in WSL or Linux

Use a clean Linux checkout/worktree of the selected revision, or CI. The current
working checkout has unrelated staged/untracked files; those are not part of this
release. Do not copy the repository onto the Windows filesystem. Python 3, Java 21
and Maven are required for this build; Node/npm are not required.

From the selected repository root:

```bash
set -euo pipefail
python3 scripts/customization/build-homepage.py
python3 scripts/customization/build-footer.py
python3 scripts/customization/build-homepage.py --check
python3 scripts/customization/build-footer.py --check
mvn clean package -DskipTests -DskipUnitTests -DskipIntegrationTests
```

Wait for `BUILD SUCCESS`. This packaging command explicitly skips automated Java
tests; it is not evidence that a release passed tests. Run the site's normal
release checks and staging smoke tests before production rollout.
Do not use an old WAR left in `target/` from the development setup: FRD updates an
exploded deployment and does not regenerate that old WAR.

Check the newly packaged resources/default locale:

```bash
python3 - <<'PY'
from pathlib import Path
from zipfile import ZipFile
war = Path('target/dataverse-6.12.war')
with ZipFile(war) as z:
    for folder in ('resources/images/uard/', 'resources/js/uard/'):
        files = [p for p in Path('src/main/webapp', folder).rglob('*') if p.is_file()]
        assert files, f'Missing source directory: {folder}'
        for p in files:
            name = p.relative_to('src/main/webapp').as_posix()
            assert z.read(name) == p.read_bytes(), f'Stale/missing WAR resource: {name}'
    assert b'<default-locale>ua</default-locale>' in z.read('WEB-INF/faces-config.xml')
print('WAR contains the matching UaRD resources and Ukrainian default locale.')
PY
```

Create an upload archive with only the deployable artifacts:

```bash
RELEASE_ID="$(git rev-parse --short=12 HEAD)"
RELEASE_DIR="$(mktemp -d /tmp/uard-release.XXXXXX)"
mkdir -p "$RELEASE_DIR/app" "$RELEASE_DIR/branding"
cp target/dataverse-6.12.war "$RELEASE_DIR/app/"
cp customization/branding/custom-homepage.html \
   customization/branding/custom-footer.html \
   customization/branding/custom-stylesheet.css "$RELEASE_DIR/branding/"
git rev-parse HEAD > "$RELEASE_DIR/REVISION"
(
  cd "$RELEASE_DIR"
  sha256sum app/* branding/* REVISION > SHA256SUMS
)
tar -C "$RELEASE_DIR" -czf "/tmp/uard-6.12-${RELEASE_ID}.tar.gz" .
printf 'Upload: /tmp/uard-6.12-%s.tar.gz\n' "$RELEASE_ID"
```

The archive intentionally contains no native language bundles for this server.
Record the archive checksum separately in your release record.

## 4. Upload and install the runtime files

Example upload from the build machine; replace `DEPLOY_USER@SERVER`:

```bash
scp "/tmp/uard-6.12-${RELEASE_ID}.tar.gz" DEPLOY_USER@SERVER:~/
```

On the remote server, set `RELEASE_ID` to the uploaded archive's revision:

```bash
set -euo pipefail
RELEASE_ID='REPLACE_WITH_REVISION'
REMOTE_RELEASE="$HOME/uard-releases/$RELEASE_ID"
mkdir -p "$REMOTE_RELEASE"
tar -xzf "$HOME/uard-6.12-${RELEASE_ID}.tar.gz" -C "$REMOTE_RELEASE"
(cd "$REMOTE_RELEASE" && sha256sum -c SHA256SUMS)
```

Deploy during the site's normal maintenance window. **Install the new WAR first,
then publish its matching branding fragments.** The fragments reference the new
resource files with content-version query strings. Serve both parts from the same
release; maintenance avoids visitors seeing a mixed release during replacement.

### A. Direct Payara installation

Use the installed `asadmin`, its normal secure authentication and the existing
application name/domain/target. Run as the authorized Payara administrator; do not
use the development script's hardcoded admin password.

```bash
ASADMIN='/path/to/payara/bin/asadmin'
APP_NAME='REPLACE_WITH_EXISTING_APPLICATION_NAME'
"$ASADMIN" deploy --force=true --name "$APP_NAME" --contextroot / \
  "$REMOTE_RELEASE/app/dataverse-6.12.war"
```

These examples assume the default administration connection/target. Add your
site's established connection, password-file and target options where required.
Check deployment success and Payara logs before publishing the branding files.
Keep existing database, Solr, storage, mail, DOI and authentication configuration.
Do not rerun the Dataverse installer or import the local development database.

Then install the three branding files to the paths used in section 5:

```bash
sudo install -d -m 0755 /var/www/dataverse/branding
sudo install -m 0644 "$REMOTE_RELEASE/branding/custom-homepage.html" \
  "$REMOTE_RELEASE/branding/custom-footer.html" \
  "$REMOTE_RELEASE/branding/custom-stylesheet.css" /var/www/dataverse/branding/
```

These files contain public branding, so read-only files are sufficient. Confirm
the Payara service user can traverse the directories and read the files. Adapt
ownership/security labels to the host's existing policy; writable permissions for
the application process are not required for branding.

### B. Docker/container installation

Use the remote server's existing image/WAR deployment mechanism. The local
`dev_dataverse` name, `gdcc/dataverse:unstable` image and FRD compose configuration
are development details, not a production deployment recipe.

1. Put the customized WAR into your established application image or persistent
   deployment volume. Record/pin the resulting image digest. An ad-hoc `docker cp`
   into a running container alone is lost when the container is recreated.
2. Publish the three branding files to a persistent host directory.
3. Add the following mounts to the **production application's** compose/service
   configuration, adjusting the service and host directory names:

```yaml
services:
  YOUR_DATAVERSE_SERVICE:
    volumes:
      - /srv/uard/branding:/var/www/dataverse/branding:ro
      # Keep any existing language mount and JVM option unchanged.
```

4. Deploy/recreate only the necessary application service using the site's normal
   procedure. Preserve database, search and file-storage volumes. Do not run
   `docker compose down -v`.
5. Confirm the mounts are visible/readable inside the application container.
   Filesystem paths in Dataverse settings refer to **container paths**, not host
   paths. Run local admin API calls inside that container if necessary.
6. Configure JVM options through the persistent container configuration/startup
   mechanism. A manual option in an ephemeral container must survive recreation.

An exact Docker WAR/image command depends on your current production image and
entrypoint. Use its documented mechanism; do not assume `/opt/payara/deployments`
from local development is the remote deployment directory.

## 5. Configure Dataverse settings

The supported file hooks take **absolute filesystem paths**, not browser URLs or
uploaded file contents. These settings are stored in the remote database. The
[official Dataverse 6.12 branding documentation](https://guides.dataverse.org/en/6.12/installation/config.html#branding-your-installation)
describes the hooks used here.

Run on the server/application container where local admin access is permitted:

```bash
LOCAL_URL='http://localhost:8080'
curl --fail-with-body -X PUT \
  --data-binary '/var/www/dataverse/branding/custom-homepage.html' \
  "$LOCAL_URL/api/admin/settings/:HomePageCustomizationFile"
curl --fail-with-body -X PUT \
  --data-binary '/var/www/dataverse/branding/custom-stylesheet.css' \
  "$LOCAL_URL/api/admin/settings/:StyleCustomizationFile"
curl --fail-with-body -X PUT \
  --data-binary '/var/www/dataverse/branding/custom-footer.html' \
  "$LOCAL_URL/api/admin/settings/:FooterCustomizationFile"
```

Check every response for success. These three values replace existing branding
settings: review the current values before applying them. **Keep `:Languages`
unchanged on this already-localized server.** For a Docker
installation, execute the same calls in the application's network context using
its installed HTTP client and existing admin-access policy.

Additional configuration:

| Item | Required action |
| --- | --- |
| `:HeaderCustomizationFile` | No hook is needed for our styled header. Do not enable the local dummy file. Review any existing remote custom header before removing it. |
| `:LogoCustomizationFile` | No change required; the current native navbar brand is retained. UaRD hero/footer logos come from the WAR resources. |
| `:Languages` | Retain the existing working value. Verify that the WAR's Ukrainian default matches its locale code (`ua` or `uk`). |
| `dataverse.lang.directory` | Retain the existing option and translation directory; verify native translations after WAR replacement. |
| Root featured collections | Curate published remote collections; see section 7. |
| `:ApplicationPrivacyPolicyUrl` | Optional: set to an approved policy URL if the native legal strip should display it. |
| `:FooterCopyright` | Optional: set approved extra legal text using the existing native setting. |
| `:NavbarSupportUrl` | Preserve the site's support setup. The custom footer reuses the native support dialog when its JSF action exists. An external support URL does not currently create a custom-footer support link. |

Our footer uses the supported custom-footer hook plus CSS: dark-blue columns
appear above a thin native copyright/privacy row, and the standard logo/version
column is hidden. No upstream footer template replacement is required.

## 6. Preserve and verify existing localization

Localization is already configured on the target. Skip language installation:

- Do not copy our local `customization/lang/` files.
- Do not create or replace the language-directory JVM option.
- Do not overwrite `:Languages` or reimport metadata translation bundles.
- Preserve the current translated UI and metadata files, including any custom
  Knowledge Area/Clusters Subject translations.

Before deployment, record the existing `:Languages` response and language-directory
option using section 2. Check the bundle files on the server, without changing
permissions, contents or naming. The packaged first-visit locale must use the
same Ukrainian code. The current local configuration is `ua`; this is not evidence
that the remote configuration uses that code.

After replacing the WAR, select both languages and verify native header, search,
collection and dataset screens as well as the custom homepage/footer. Compare
translation coverage with the pre-deployment site. A missing translation after
deployment should first be investigated as a configuration/bundle-loading issue;
do not replace working server bundles with our incomplete local copies.

No localization-only restart is needed when its files/JVM options remain unchanged;
normal application redeployment still applies. If the console checks reveal an
unconfigured or incompatible setup, plan that correction separately before rollout.
The [Dataverse language-directory documentation](https://guides.dataverse.org/en/6.12/installation/config.html#configuring-the-lang-directory)
describes the configuration mechanism.

## 7. Configure the remote homepage collections and destinations

The target root featured list was empty when checked. Configure it if the new
homepage should display institution cards. Existing public collections can be
featured without recreating or migrating them.

The institutions strip reads these public, same-origin APIs:

```bash
curl --fail-with-body "$LOCAL_URL/api/dataverses/:root/featured"
# For each alias returned above:
curl --fail-with-body "$LOCAL_URL/api/dataverses/REPLACE_WITH_ALIAS"
```

Open the root collection's native page, then choose **Edit → Featured Dataverse
Collections** to select real published subcollections and save. The homepage
uses that same featured list and its stored order. If managing the list with API calls, use the
native featured-dataverses API with a **user API token** having the required
collection permissions. Server shell access does not replace that user token.

The frontend sends no token and filters out unreleased collections. Verify these
APIs work anonymously through the public hostname as well as locally. Collection
names are stored data and are not automatically translated; the current cards
use generic Dataverse icons, not institutional logo uploads.

Do not run development seed scripts on production by default. Local examples,
`test-university`, accounts, uploaded logos and featured-list database values are
not transported by the WAR or branding files.

Knowledge areas (11 cards) and clusters (15 cards) are currently **non-navigating
placeholders**. Their “View all” links open native collections browsing. Configure
real taxonomy/filter URLs in a separate reviewed change before presenting those
cards as active navigation.

Footer destinations in `customization/footer/links.json` are currently `null`, so
most entries are labels. Articles/pages/CMS are not implemented by this release.
To activate a destination, supply a verified root-relative or HTTPS URL, regenerate
`custom-footer.html` and publish that fragment. Language-specific article URLs
will need a content/link mapping change when those pages are added.

## 8. Verify before ending maintenance

HTTP/resource checks (use the public URL as well):

```bash
PUBLIC_URL='https://uard.dntb.gov.ua'
curl --fail-with-body "$PUBLIC_URL/api/info/version"
curl --fail-with-body -o /dev/null "$PUBLIC_URL/resources/js/uard/homepage.js"
curl --fail-with-body -o /dev/null "$PUBLIC_URL/resources/js/uard/footer.js"
curl --fail-with-body -o /dev/null "$PUBLIC_URL/resources/images/uard/uard-logo.png"
curl --fail-with-body -o /dev/null "$PUBLIC_URL/resources/images/uard/hero-background.webp"
curl --fail-with-body "$PUBLIC_URL/api/dataverses/:root/featured"
```

In a fresh private browser session:

- First visit starts in Ukrainian, including custom homepage/footer. Select English,
  navigate to another page and back, and switch to Ukrainian again.
- Hero/logo/icons load with no 404s or console errors. Check requests with their
  generated `?v=` values, not just the unversioned URLs above.
- Basic search preserves English/Ukrainian text; Advanced search opens the native
  page; collection cards and “View all” open expected native destinations.
- All strips scroll both ways and reach their ends, with disabled edge buttons.
  Test more than five featured collections in staging. At 320/375px each mobile
  strip shows one complete card without a cropped adjacent card.
- Header login/logout, user menu, notifications, language selector and Add Data
  permissions behave correctly for logged-out, normal-user and admin sessions.
- Existing native Ukrainian translations still render without missing-key errors,
  and custom metadata/controlled-vocabulary labels retain their previous coverage.
- Footer appears on homepage and native pages, with no blank gap underneath; native
  legal/privacy behavior remains available. Test native support without sending
  a test message to real recipients. Check widget pages separately.
- Existing dataset downloads and normal authenticated workflows still work.

Inspect Payara logs for deployment/bundle errors. If a proxy/CDN caches HTML or CSS,
refresh/invalidate the relevant cache using the site's existing procedure. Resource
query versions change when builders see modified images/scripts; a proxy that
ignores query strings may also require cache invalidation.

## 9. Roll back if verification fails

During maintenance, restore the previous WAR/image **and its matching branding
files** as one release, then restore the changed settings from the private backup.
If a setting was previously absent, remove that specific setting instead of
writing an empty value. Restore any changed language directory/JVM option and
restart as needed. Do not import the local development database or restore the
whole production database for a branding-only rollback.

Example only when the homepage hook was absent before deployment:

```bash
curl --fail-with-body -X DELETE \
  "$LOCAL_URL/api/admin/settings/:HomePageCustomizationFile"
```

Repeat verification before reopening traffic. Retain both release artifacts,
checksums and the final settings/change record for the next deployment.

## 10. Future updates: which deployment action is needed?

| Change | Action |
| --- | --- |
| `custom-stylesheet.css` only | Publish CSS; browser/cache refresh |
| Homepage/footer text, markup or configured footer URLs | Run the relevant Python builder and publish generated HTML; no WAR deployment if JS/images are unchanged |
| `homepage.js`, `footer.js`, images or SVGs | Run builders, rebuild/deploy WAR, then publish matching HTML fragments |
| `faces-config.xml` or another application source file | Rebuild/deploy WAR; document upstream changes |
| Native `.properties` bundles | Update reviewed external files and restart/redeploy to clear caches; no WAR rebuild for external bundles |
| Featured collections | Change the remote native featured list; no file deployment |

Further repository references: [branding deployment](branding-deployment.md),
[resource assets](resource-assets.md), [custom footer](custom-footer.md),
[upstream modifications](upstream-modifications.md).

## Incremental bilingual About-page deployment (2026-10-11)

This section supersedes the older statements that no article pages exist or that
About is Ukrainian only. The local /about.xhtml route now selects Ukrainian or
English editorial content using the native Dataverse locale and shared JSF shell.
Local integration does not establish that these changes reached the remote server.

For this bilingual update, deploy the two fragments first, then the wrapper,
preserving the following paths inside the confirmed active application:

```text
<active-deployed-app>/uard/content/about.uk.xhtml
<active-deployed-app>/uard/content/about.en.xhtml
<active-deployed-app>/about.xhtml
```

Then publish the generated customization/branding/custom-footer.html to the existing
branding path. It removes the fixed Ukrainian hreflang declaration while retaining
one /about.xhtml link. See [custom-about.md](custom-about.md#one-url-two-article-languages)
for implementation, content editing and verification. This update changes no CSS,
JavaScript resources, language bundles or API settings when the preceding UaRD/About
baseline is already installed.

For the first About-page installation, also publish the integrated custom-stylesheet.css.
The preceding hero changes require custom-homepage.html. If the earlier institution-logo
update is absent remotely, deploy its generated resources/js/uard/homepage.js before
the matching homepage HTML. Use integrated repository artifacts, not the older supplied
HTML/CSS copies, to retain the logo fixes.

For copy-based deployment, ask the administrator to confirm the active application
root containing dataverse_template.xhtml and WEB-INF, and make these files readable
by the Payara service user. Check /about.xhtml after installation. A full Payara
restart is not always required; reload/redeploy the Dataverse application if the
runtime does not discover the views after checking paths/permissions. Include all
three XHTML files in the maintained deployment artifact so future redeployments
retain them. Packaged deployments require a rebuilt WAR through the normal release
procedure. Preserve remote native language bundles, locale settings and server-specific
application changes. Verify UA -> EN -> UA, title/description, article language and
footer navigation before considering the deployment complete.

## Incremental top-menu system summary (2026-10-11)

The new short article is /system.xhtml; the long bilingual footer article remains
/about.xhtml. With the existing UaRD/About baseline, copy
src/main/webapp/uard/content/system.uk.xhtml to
<active-deployed-app>/uard/content/system.uk.xhtml first, then copy
src/main/webapp/system.xhtml to <active-deployed-app>/system.xhtml.
No CSS/JS/branding fragments change in this update.

Verify the page before configuring :NavbarAboutUrl to the fully-qualified public
URL https://uard.dntb.gov.ua/system.xhtml. This native setting creates the top-menu
About entry and must be applied on the remote server separately; the local setting
is not migrated with Git. The page shows Ukrainian text in either native UI language
until an English version is supplied. Preserve remote native translations and locale
settings. See [custom-system.md](custom-system.md) for the exact admin API calls,
optional maintained-bundle menu wording and application reload guidance.

## Article header-spacing CSS update

Publish the integrated customization/branding/custom-stylesheet.css to the existing
remote StyleCustomizationFile path (normally /var/www/dataverse/branding/custom-stylesheet.css).
Refresh the browser or relevant CSS/HTML cache. No application restart, XHTML or
JavaScript deployment is needed for this change. It extends the existing empty
message-panel margin exception to both article pages and retains the 64px fixed
navbar offset. Panels containing messages keep their native spacing.

## Incremental ecosystem footer articles (2026-10-11)

Deploy the four uard/content/{eosc,openaire,re3data,fair}.uk.xhtml fragments first,
then the four corresponding {eosc,openaire,re3data,fair}.xhtml wrappers into the
confirmed active application, preserving paths under src/main/webapp. Check their
public routes before publishing the generated customization/branding/custom-footer.html
to the existing configured branding path. No API setting, native language bundle
or resource JS change is required when the UaRD baseline is installed.

All four articles currently show supplied Ukrainian text in either interface
language. Their footer links carry hreflang="uk". Reuse existing article styles;
the packaged current stylesheet is only needed if the preceding styling/header-gap
baseline has not reached the server (merge any remote stylesheet additions).
See [ecosystem-pages.md](ecosystem-pages.md) for the full manifest, reload guidance
and future English content pattern. Remote deployment was not performed.

## Footer spacing/color CSS update (2026-10-11)

Publish the current integrated customization/branding/custom-stylesheet.css to the
existing StyleCustomizationFile path and refresh the browser/cache. This restores
compact matching linked/pending rows and the original muted #b9ccdc footer color,
with existing hover/focus states and consistent 44px rows on small/coarse-pointer
screens. This CSS-only update does not require an application restart or new
HTML/JS/API settings.

## Incremental DNTB operator articles (2026-10-11)

Deploy uard/content/contacts.uk.xhtml and about-dntb.uk.xhtml first, followed by
their contacts.xhtml and about-dntb.xhtml wrappers in the confirmed active application
root. Check both routes before publishing the generated custom-footer.html. This
update reuses the current article/footer styles and adds no CSS/JS/API changes.
The generated footer includes the earlier ecosystem-page links, so those pages
must already be available too. A combined footer-article package includes all six
pending Ukrainian pages and the current footer/styles. See
[operator-pages.md](operator-pages.md) for the manifest and copy/reload procedure.
Keep remote native localization and stylesheet additions; no remote deployment
was performed by this task.

## Incremental Policies overview (2026-10-11)

Copy uard/content/policies.uk.xhtml into the active application's content folder,
apply the current custom-stylesheet.css (with its scoped hierarchical-numbering
rule), then copy policies.xhtml to the application root. Check the page before
publishing generated custom-footer.html. Preserve remote stylesheet additions
when merging. The page renders supplied Ukrainian text in either UI locale and
identifies its content language with uk. No admin setting or resource JS/native
language-bundle change is required. See [policies-page.md](policies-page.md).
The refreshed combined footer package includes all seven current Ukrainian footer
articles and the matching CSS/HTML. Remote deployment was not performed.
