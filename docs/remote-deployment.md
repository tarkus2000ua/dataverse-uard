# Deploying the UaRD customization to a remote Dataverse 6.12 server

This guide deploys the current homepage, header styling, collection strips and
custom footer onto an **existing Dataverse 6.12 installation**. It does not install
a new Dataverse instance or migrate the local development database.

Historical baseline: commit `1c5b70e053` on `codex/homepage-hero` (2026-10-04).
For current work use the reviewed `codex/login-local-guide` revision and the
2026-11-01 incremental section below. Keep the WAR,
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

Keep the target's existing localization configuration.

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

### Enable UaRD MIME type detection

Install [the MIME detection bundle](../customization/lang/MimeTypeDetectionByFileExtension.properties)
in the directory already configured by `dataverse.lang.directory`. If that
directory already has a MIME detection bundle, merge the UaRD entries into it
and retain the site's existing mappings. Keep the packaged Dataverse mappings
in the external base bundle because Dataverse loads it as a replacement.
Refresh those mappings when upgrading Dataverse.

Restart Payara to reload the bundle. A WAR rebuild is unnecessary. Verify the
seven recommended extensions through Native API multipart upload with the file
part set to `application/octet-stream`. Check `dataFile.contentType` in the
draft file listing. For existing files, use
`POST /api/files/{id}/redetect?dryRun=true` to review the detected type before
saving it.

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

## Current uard2 guide, logo and login updates (2026-11-01)

This section supersedes historical statements that the navbar source is unchanged
or that local bundles are still the original community baseline. Target test
host: **https://uard2.dntb.gov.ua/**. The agent has not deployed to this host.
Keep its actual Payara application path, local admin port, language filename,
provider configuration and existing data. Publishing Git does not deploy it.

| Update | Files/settings to merge or install | Reload |
| --- | --- | --- |
| Ukrainian terminology | Reviewed bundle entries and the three My Data presentation files in ukrainian-collection-terminology.md | Existing application reload; browser refresh |
| User Guide | Generated src/main/webapp/guide/ (eight HTML pages and pics/1.jpg through 4.jpg), plus /guide/ PrettyFaces mapping | Normal application deployment/reload for routing |
| Native guide menu | :NavbarGuidesUrl=https://uard2.dntb.gov.ua/guide/ | Setting update; retain trailing slash |
| Logo product name | One-line default navbar-brand change in dataverse_header.xhtml: replace its bundle.dataverse expression with literal Dataverse | Normal view/application reload |
| Restored ORCID guide link | org.login.orcid.blurb in active external English and Ukrainian bundles | Clear bundle caches through normal application reload |

Find the active application on the server using its normal admin connection:

```bash
asadmin list-applications --long
asadmin get 'applications.application.*.location'
```

Use the enabled deployment's location, whose root contains dataverse_template.xhtml
and WEB-INF. Do not mistake the checkout, branding directory or an inactive copy
for the active application. If location contains a Payara variable, resolve it
against the domain being administered.

For the guide, deploy **generated** src/main/webapp/guide/, not the editable
customization/guide/. Visible {{guideContent}} means a source template is served.
Merge this mapping inside the existing WEB-INF/pretty-config.xml root element,
preserving every other mapping and avoiding duplicate IDs/patterns:

```xml
<url-mapping id="uard-user-guide">
    <pattern value="/guide/" />
    <view-id value="/guide/index.html" />
</url-mapping>
```

If Nginx/Apache already serves /guide/ from another static directory, confirm that
active route and place the complete generated pages there instead. Keep files
in the maintained remote artifact so the next deployment retains them.

Set the native menu on uard2 with the existing admin API procedure and its actual
local port (8080 in this example):

```bash
curl --fail-with-body -X PUT \
  --data-binary 'https://uard2.dntb.gov.ua/guide/' \
  'http://localhost:8080/api/admin/settings/:NavbarGuidesUrl'
```

The restored login link is bundle-only and uses root-relative /guide/, `_blank`
and noopener. No additional login XHTML edit is needed. Exact English/Ukrainian
entries are in [ORCID login](orcid-login.md#local-documentation-link-restored-2026-11-01).
Merge that single key into current server bundles, retaining `{3}` for support.
The user may choose full-file replacement with current customization/lang files
after backing up and preserving remote-only edits; upload Ukrainian under the
server's existing Bundle_ua.properties or Bundle_uk.properties name. Earlier
ORCID archives predate this link update. Do not alter locale/JVM/provider settings.

Verify /guide/, direct chapters and section hashes, refresh/Back/Forward, the
four translated images and the native menu's new-tab behavior. Check the logo
in both locales and ORCID login's guide/support links on the existing enabled
server. Local formatting checks pass, but local ORCID is unconfigured and
end-to-end ORCID-only layout/callback remains a test-server check.

This update requires no database import/schema change, metadata migration, Solr
reset or Docker volume recreation. Dev-start/FRD helpers belong to the local
workspace and are not production deployment commands. Back up changed files and
prior setting values for rollback; do not restore a database for a text/link fix.

## Confirmed uard2 language-bundle update (2026-11-15)

At the user's request, the active external bundles on host `uard2` were updated
from the local working copies on November 15, 2026, at 16:37 Europe/Kyiv.
The preceding comparison found no remote-only keys in these three files.
The server's existing `uk` locale and `/dv/langBundles` directory were retained:

| Local source under customization/lang | Installed file under /dv/langBundles |
| --- | --- |
| Bundle.properties | Bundle.properties |
| Bundle_ua.properties | Bundle_uk.properties |
| citation_ua.properties | citation_uk.properties |

The release directory is
`/home/designer/uard-releases/lang-bundles-20261115T143514Z/`.
It contains the full pre-update language-directory archive
`before-langBundles.tar`, the uploaded files and checksum plan, and the
`before.json`, `installed.json` and `verified.json` audit records. Installation
preserved each file's owner, group and mode. All other files in the language
directory were verified unchanged; the English citation and MIME mappings
already matched local values semantically and were not replaced.

Only the existing `payara_domain1.service` was restarted to clear bundle caches.
Post-restart checks confirmed the three installed files match their local
sources byte for byte, all 65 audited directory files match the installation
record, and the version API reports Dataverse 6.12. A public-browser native
Ukrainian → English → Ukrainian round trip displayed the corresponding
`Авторське право` / `Copyright` footer text. The live public advanced-search
page also rendered `Куратор`. Authenticated dataset editing was not exercised.

This deployment updated external language bundles only. The earlier local
description-help XHTML removal and title-prefix-button CSS rule are separate
presentation changes and were not part of this upload. No WAR, metadata schema,
database settings or locale configuration was replaced.

To roll back only these three files, extract `langBundles/Bundle.properties`,
`langBundles/Bundle_uk.properties` and `langBundles/citation_uk.properties` from
the retained archive into `/dv`, preserving ownership and permissions, then
restart the same Payara service and repeat the language/version checks.

## Confirmed uard2 email-guide update (2026-11-15)

At the user's request, the five email templates containing documentation links
were updated on uard2 on November 15, 2026, at 18:50 Europe/Kyiv. Welcome emails
link to the guide index; collection and dataset creation emails link to their
management chapters; both ingest completion notices link to the tabular-files
section. All links use the configured Dataverse site address.

The release directory is
`/home/designer/uard-releases/email-guide-20261115T164609Z/`.
The active application remains
`/usr/local/payara7/glassfish/domains/domain1/applications/dataverse/`.
Only four files were installed:

- `WEB-INF/classes/edu/harvard/iq/dataverse/MailServiceBean.class`.
- The five email entries in `WEB-INF/classes/propertyFiles/Bundle.properties`.
- The five email entries in `/dv/langBundles/Bundle.properties`.
- The five email entries in `/dv/langBundles/Bundle_uk.properties`.

The mail class was compiled against the installed uard2 libraries. Its baseline
bytecode matched the committed source before the patch, and its enum helper
remained identical. Bundle updates merged only the reviewed entries, preserving
all other content, file ownership and modes. The installed class is byte-identical
to the locally validated compiled class. The existing `uk` locale was retained.

The release retains `before-app.war`, `before-langBundles.tar`, source inputs,
the checksum plan, and `before.json`, `prepared.json`, `installed.json` and
`verified.json` audit records. It also contains a complete updated application
artifact, `dataverse-email-guide-after.war`, with SHA-256
`d8a56bec96d4ee30d768d06c89e6910adae08fe17bd541e566ca243cf3cb1fc8`.
Use this artifact with the updated external bundles when redeploying this
uard2 revision; an older WAR would restore the previous mail class.

The existing `payara_domain1.service` was restarted. Post-startup audit verified
all 2,016 application files and 65 language files against the plan, with no
unrelated content changes. All 15 installed English/Ukrainian/fallback template
renders passed; support and failed-file details remained resolved. Public and
local version APIs returned 6.12, and the guide index, management chapters and
tabular-files anchor were verified. Local validation had passed 65 existing
unit tests and 45 template renders. No real SMTP message was sent.

To roll back this update, stop the same Payara service, restore only the two
application files from `before-app.war` and the two external bundles from
`before-langBundles.tar`, preserving their recorded metadata, then start Payara
and repeat the version, guide and template checks. No database restore is needed.

## Confirmed uard2 publication identifier update (2026-11-19)

At the user's request, the Related Publication identifier-type dropdown was
updated on uard2 to offer doi, handle, url, isbn, issn and eissn for new
selections. An existing legacy type remains selectable in its own row.

Only `metadataFragment.xhtml` in the active application at
`/usr/local/payara7/glassfish/domains/domain1/applications/dataverse/` was
patched. The server's existing description-help paragraph, which differs from
the local baseline, was retained. File ownership and mode remain
`dataverse:dataverse`, `0644`. No metadata-block reload or database change was
needed. The existing `payara_domain1.service` was restarted.

The release directory is
`/home/designer/uard-releases/publication-identifiers-20261119T095121Z/`.
It retains the original and installed XHTML, the checksum plan, the
`before.json`, `installed.json` and `verified.json` audit records, and public
HTTPS/runtime-expression check results. The installed XHTML SHA-256 is
`3b9edb80caff94abd7721dabd58344f6272501b7d70f228dfba18328303de296`.

The post-restart audit verified all 2,016 application files, with only this
XHTML changed. All 20 publication-choice checks passed against the installed
XHTML and server Jakarta EL libraries. Public HTTPS checks passed for the
version API (6.12), homepage, root collection and existing DataCite test dataset.
Authenticated browser editing was not exercised.

To roll back, restore `before-metadataFragment.xhtml` to the active application
with owner/group `dataverse:dataverse` and mode `0644`, restart the same Payara
service and repeat the version/page checks. No database restore is needed.

## Confirmed uard2 video guides deployment (2026-11-19)

Deployed on November 19, 2026, at 13:34 Europe/Kyiv from feature commit
`e5be463b4c7e84ba8899bceef3a3a89d112ae217`.

Public page: https://uard2.dntb.gov.ua/video-guides.xhtml.
The native User Guide dropdown links to the written guide and video guides.
All three supplied recordings and preview images are served by this installation.

Release and rollback directory:
`/home/designer/uard-releases/video-guides-20261119T112940Z/`.

The active application is
`/usr/local/payara7/glassfish/domains/domain1/applications/dataverse/`.
Sixteen files were installed: four new Facelets files, six media assets, the
existing header, web.xml, packaged English fallback, two external language
bundles and the configured `/dv/design/custom-stylesheet.css`.

The header was patched around the server's existing conditional written-guide
links, retaining their rendered checks, settings expression and new-tab behavior.
Only six player/menu keys were appended to the packaged English fallback and
`/dv/langBundles/Bundle{,_uk}.properties`. Only the scoped video CSS was appended
to the active stylesheet. The working `uk` locale and all other server
customizations were retained; local Java classes and complete local bundles
were not uploaded.

The release retains `before-app.war`, `before-langBundles.tar`,
`before-stylesheet.css`, committed source inputs, the installation helper,
`plan.json`, `before.json`, `prepared.json`, `installed.json` and public HTTPS
checks. The maintained application artifact is
`dataverse-video-guides-after.war`, SHA-256:
`26d0d9ad1a6a4b90cc1917c68ccf5253ed3edb73f6ab8a8609c2233729e7ae55`.
Use that artifact together with the merged external bundles and stylesheet for
this server revision; an older WAR would omit the page and submenu.

The existing `payara_domain1.service` was restarted. Its version API reports
Dataverse 6.12. The audit verified all 2,026 application files and 65 language
files, plus the stylesheet, against the exact installation plan. Unrelated
file contents, ownership and modes were preserved. Public HTTPS returned 200
for the page and posters and 206 with video/mp4 for byte ranges on all three
videos. The page renders three players and both Ukrainian submenu labels;
the removed recording-language/audio sentence is absent.
No additional browser checks were run.

For rollback, stop the same Payara service, restore the three changed existing
application files from `before-app.war`, remove only the ten application files
added by this release, restore the two external bundles and stylesheet from
their backups, then start the service. Use `prepared.json` for exact paths and
original ownership/modes. No database restore or settings change is required.

## Cropped video replacement on uard2 (2026-11-19)

The approved 1056×594 (16:9) crops and matching posters replaced the six existing
media files on November 19 at 13:52 Europe/Kyiv. Original media URLs were retained;
the original shared player template was restored and its cached view reloaded by
13:59. The final state changes only the six media files.

Release: `/home/designer/uard-releases/cropped-video-guides-20261119T115217Z/`.
The release contains the approved asset hashes in `plan.json`, original media in
`before-media.tar`, the original player backup, deployment/audit records and the
maintained `dataverse-cropped-video-guides-after.war`. Its SHA-256 is:
`08c1b5163e9651dd82ca330bad58d84fbf018551cf1319bf6aaf0187bb6ed6b0`.

The final audit checked all 2,026 application files, 65 language files and the
stylesheet. Only the six media files differ from the pre-update snapshot;
ownership, modes and all other contents are preserved. The app reports 6.12 and
renders three players using the original URLs. No media URL version parameters
remain. The crops were deployed from the approved working-tree assets before
their later Git publication with the narrated-video revision.

Cloudflare still served the earlier media from its edge cache during the final
public check (`HIT`, `max-age=14400`). A purge of the three MP4 and three JPEG
URLs under `/resources/videos/uard/`, or normal cache expiry, is needed for those
cached responses to show the cropped files. Origin checks match the approved
asset hashes. No Cloudflare cache purge was performed.

For rollback, atomically restore the six files from `before-media.tar`, retaining
their recorded owner/group/mode. No database, language, menu or stylesheet restore
is required. Keep the updated WAR with the installation's existing external files.

## Narrated video guides on uard2 (2026-11-22)

The corrected Ukrainian and English narration versions of all three videos were
deployed at 19:51 UTC (21:51 Europe/Kyiv) on November 22. The public page remains
https://uard2.dntb.gov.ua/video-guides.xhtml.

Release and rollback directory:
`/home/designer/uard-releases/narrated-video-guides-20261122T194815Z/`.
The release records the approved, uncommitted working-tree revision based on
`6d5cf2440f1b2fa0edd652d73e4b7f1e2d11ca17`; this deployment did not publish Git changes.

Ten files were installed in the existing Payara application/external bundles:
the three original `.mp4` URLs now contain Ukrainian narration, three new
`-en.mp4` files contain English narration, the shared player selects the file
using `dataverseSession.localeCode`, and one player-language key was updated in
the packaged English fallback and `/dv/langBundles/Bundle{,_uk}.properties`.
Only that key was merged into each remote bundle. The native `uk` locale,
posters, header, stylesheet, unrelated files and file ownership/modes were retained.
Video dimensions remain 1056×594 (16:9). No versioned URLs were added.

The release contains `before-changes.tar`, `before.json`, the approved source
archive and checksums, installation helpers, `prepared.json`, `installed.json`
and public HTTPS check results. The maintained artifact is
`dataverse-narrated-video-guides-after.war`, SHA-256:
`54d3a5114389eb749490533959d48066de7aeb3ca6c4010b04e804dc7d7cfc67`.
Use it with the merged external bundles retained in `updates/lang/`.

The existing `payara_domain1.service` restarted successfully. The audit checked
all 2,029 application files, 65 language files and the configured stylesheet;
only the planned files changed. An anonymous native English → Ukrainian locale
round trip selected all three matching audio versions. The origin returned
HTTP 206/video/mp4 with the approved bytes for all six videos, and the version
API returned 6.12. Public HTTPS returned 200 for the page and 206 for all six
video URLs, matching the approved lengths and initial bytes. Cloudflare fetched
the new content (`EXPIRED` for the original URLs, `MISS` for English); no purge
or query-string cache workaround was required. No browser checks were repeated.

For rollback, stop the same Payara service, restore the seven existing files
from `before-changes.tar` into their recorded application/language locations,
remove only the three English MP4s added by this release, then start the service.
Use `before.json` and `prepared.json` for exact paths, ownership and modes.
No database, settings, header or stylesheet restore is required.

## University registration demo on uard2 (2026-11-24)

At the user's request, the university invitation and demonstration registration
form were deployed on November 24 at 23:55 Europe/Kyiv (21:55 UTC). Public routes:
https://uard2.dntb.gov.ua/#dv-institutions and
https://uard2.dntb.gov.ua/institution-registration.xhtml.

Release/rollback directory:
`/home/designer/uard-releases/university-registration-demo-20261124T215021Z/`.
The approved, uncommitted source is on `codex/university-registration`, based on
`f9ab231edb`. This deployment did not commit, merge or push the local work.

Seven files were installed:

- New `institution-registration.xhtml` in the existing application at
  `/usr/local/payara7/glassfish/domains/domain1/applications/dataverse/`.
- Three new `WEB-INF/classes/propertyFiles/UardInstitutionRegistration*.properties`
  bundles: English fallback, legacy `ua` and the target's existing `uk` locale.
- The configured `/dv/design/custom-homepage.html`, `custom-footer.html` and
  `custom-stylesheet.css`, merged with their current server contents.

The invitation follows the three existing carousels. Its explanation remains
visible; the hero action scrolls to it, and its primary action opens the new form.
The form contains 17 required fields plus consent and identifies itself as a demo.
Submission is blocked, fields have no submission names, and no application, logo
or personal data is sent or stored. Database intake and notifications are deferred.

The server's existing editorial strings, image/script versions, native templates,
language files, `uk` configuration and unrelated stylesheet content were retained.
There were no Java/class, servlet, database/schema, mail or authentication changes.
The maintained WAR was assembled from the active server application, not the
unrelated local working-tree Java changes.

The release retains `source.tar.gz`, its checksum plan, installation/runtime
helpers, `before.json`, `before-changes.tar`, `prepared.json`, `installed.json`,
and external HTTPS/browser checks in `public.json`. The updated artifact is
`dataverse-university-registration-demo-after.war`, SHA-256:
`9cca24aabad53bfcd7e89ba9c7515e1ea9587eabc6218efcda7ddf383154f547`.
Use it together with the release's merged `updates/design/` files in a future
redeployment. The approved source archive SHA-256 is:
`baae7d5d448e5ba4b5caf59ed14b590507b45e906a06b34b9af3b65809c3c837`.

`payara_domain1.service` restarted successfully and reports Dataverse 6.12. The
audit verified all 2,033 application files (four added), all 65 language files
and all five design files; only the seven planned files changed, preserving
ownership/modes and the four checked branding/language settings. Public HTTPS
returned 200 for homepage, registration, version API and video guides. Native
Ukrainian → English → Ukrainian switching, invitation navigation and 320px
layout without page/control overflow passed in the public browser. No application
was submitted or email sent. TLS verification was not bypassed: external checks
used Windows system trust/browser because the server's own public-HTTPS request
encountered its local self-signed certificate chain.

Rollback: stop the same service, restore the three existing design files from
`before-changes.tar` with the metadata in `before.json`, remove only the four
application files added by this release, then start the service and repeat the
version/homepage/form checks. There is no database, external-language or settings
restore for this feature.

## Functional institution registration on uard2 (2026-11-25)

The user requested deployment of the working registration form. It replaced the
demonstration on November 25 at 12:56 Europe/Kyiv (10:56 UTC). Public form:
https://uard2.dntb.gov.ua/institution-registration.xhtml.
Superuser review: https://uard2.dntb.gov.ua/institution-applications.xhtml.

Release and rollback directory:
`/home/designer/uard-releases/university-registration-live-20261125T105155Z/`.
The approved source remains uncommitted on `codex/university-registration`, based
on `f9ab231edb`; no Git publication or tag was performed by this deployment.

Twenty-two files were installed in the existing application and design directory:

- Fourteen new `WEB-INF/classes/edu/harvard/iq/dataverse/uard/Institution*.class`
  files, including nested classes.
- `WEB-INF/classes/db/migration/V6.12.0.1__uard_institution_applications.sql`.
- Updated registration view and three dedicated registration bundles, the new
  protected review view and `resources/js/uard/institution-registration.js`.
- The configured `/dv/design/custom-stylesheet.css`, retaining all existing styles
  except obsolete demo-only rules and appending functional feedback/review styles.

The WAR was assembled from the active server application at
`/usr/local/payara7/glassfish/domains/domain1/applications/dataverse/`, adding only
these feature files. Unrelated local Java/metadata changes were not deployed.
The homepage/footer, native header, language bundles, native `uk` locale, database
settings and existing SMTP configuration were retained. The support address is
`uard@dntb.gov.ua` and notification links use `https://uard2.dntb.gov.ua`.

The release contains `plan.json`, the exact `payload.tar.gz`, approved
`source-inputs.tar.gz`, installation/runtime helpers, `before.json`,
`before-app-and-branding.tar`, private `before-domain.xml`,
`before-db-prepared.dump`, a second quiescent `before-db-install.dump`,
`before-install-db.json`, `prepared.json`, `installed.json` and `public.json`.
Database/domain backups are mode 0600 inside the private release directory and
were not downloaded into the repository.

Maintained artifact: `dataverse-university-registration-live-after.war`, SHA-256:
`9c6ded6f8a49bade60784144ddea7726789374400ab4fea6a83508a44a94b357`.
Use it with the release's merged `updates/design/custom-stylesheet.css` and the
installation's existing external branding/language files.

`payara_domain1.service` restarted successfully and reports Dataverse 6.12. Flyway
applied `6.12.0.1`, adding only `uard_institution_application` and its pending-mail
index. Existing core entity counts and the complete settings digest are unchanged.
The audit verified all 2,050 application files, 65 language files and five design
files: only the 22 planned files changed, preserving ownership/modes.

Origin checks passed for English/Ukrainian rendering, 19 required controls, packaged
JavaScript, CSRF rejection, arithmetic verification, missing fields, spoofed logos,
oversized payloads and HTTP 403 for guest list/detail/logo access. No successful
application was submitted, so the new remote table remained empty and no external
test email was sent. Successful submission/idempotency, local SMTP capture and
authenticated list/detail/logo review had already passed locally. Remote SMTP
delivery and authenticated remote review were not exercised. The specifically
requested demo university remains in the local database only.

External HTTPS returned 200 for the form, homepage, version API and video guides,
using Windows system trust and browser without disabling TLS verification. Public
browser checks passed native UK → EN → UK switching, empty-form focus, desktop
layout and 320px single-column layout with no overflowing controls. The existing
three homepage carousels remain together before the invitation. Native Payara
Jersey startup warnings remained present; no feature class/method/queue errors
were reported. One early-closing guest HTTP probe caused a connection-reset warning
while rendering the native 403 page; the expected 403 response was verified.

Rollback: stop the same service. Restore the four replaced application files and
stylesheet from `before-app-and-branding.tar`, using `before.json` for exact metadata.
Remove only this release's added classes, review view and JavaScript. **Retain the
applied SQL migration file, intake table and Flyway history**, including any later
submitted applications. Restoring the old WAR without retaining that migration
causes Flyway validation to fail. Rebuild the rollback WAR with the retained SQL
file, start Payara and repeat version/demo/access checks. Do not restore the whole
database or drop the intake table as part of an application rollback.

After deployment, the user explicitly requested a demo submission on uard2.
At 13:04 Europe/Kyiv on 2026-11-25, the live form accepted the fictional
Демонстраційний університет UaRD, reference
`87172605-5d37-403f-ae22-fa9ed5d7fd1a`. Its 16 submitted text/select fields, exact
9,383-byte PNG and consent version were verified in `dvndb`; exactly one application
with the demo's English name exists. The support notification was accepted by SMTP
on the first attempt and `notification_sent_at` was set at 13:04:35 +02:00.
The release's `demo-university.json` records the result. The record is intentionally
retained, with fictional contact details and a reserved `.invalid` email address.
This provides a successful remote submission/SMTP check; inbox receipt and
authenticated remote review were not exercised. Review URL:
https://uard2.dntb.gov.ua/institution-applications.xhtml?id=87172605-5d37-403f-ae22-fa9ed5d7fd1a.
