# Move UaRD development to another Windows PC

Updated 2026-11-01. This transfers the local Dataverse 6.12 development setup,
not the production server. Use Git for source files and a private archive for
local data. Keep the new checkout in Ubuntu's Linux filesystem.

## What to obtain from Git and what to copy

| Item | Transfer method | Why |
| --- | --- | --- |
| Repository source, branding, resources, articles, language files, compose configuration, dev scripts and maintained docs | Clone `codex/login-local-guide` from `tarkus2000ua/dataverse-uard` | These are versioned |
| Entire `docker-dev-volumes/` directory | Copy a cold Linux tar archive | Local database, settings, accounts, collections, search index, logos/application data and secrets stored in bind mounts |
| Dataset storage outside those mounts, if configured | Separate private backup | A container's writable filesystem is not included in the bind-mount archive |
| Cached Docker images | Optional private `docker image save` archive; recommended for this move | Preserve working `unstable`/`latest` tags and avoid downloading changed images |
| `customization/homepage/assets/` | Optional separate archive | Original logo PDF and unused design artwork are deliberately outside Git |
| Local `.env` changes/private configuration | Copy privately only if different from Git | The baseline `.env` and compose override are already tracked |
| Maven `~/.m2/repository/` | Optional cache | Speeds builds; not required |
| `target/`, containers, Docker Desktop installation | Rebuild/recreate/install | Do not transfer an old build as the source of truth |

The unused staged `custom-header.html` is not part of the committed application.
It is not needed on the new PC. Other personal outputs outside the repository,
such as incremental deployment ZIPs under Windows Documents, can be copied
separately if wanted; current application files can be regenerated from Git.

Database settings and featured collections move with the database, not with Git.
The logos configured on collections also need their application/storage files.
Our stack uses host bind mounts rather than named Docker volumes for PostgreSQL,
Solr and `/dv`. See [Docker bind mounts](https://docs.docker.com/engine/storage/bind-mounts/)
for how those host files are exposed to containers.

## 1. Prepare the old PC

Run these commands in Ubuntu/WSL from the current repository. These are manual
instructions; creating this guide did not stop containers or create a data backup.

```bash
cd ~/projects/dataverse
set -euo pipefail
TRANSFER_DIR="$HOME/uard-transfer"
mkdir -p "$TRANSFER_DIR"
chmod 0700 "$TRANSFER_DIR"
git rev-parse HEAD > "$TRANSFER_DIR/source-revision.txt"
```

While the original container exists, record the current mounts and custom Payara
configuration. The JVM-options record may contain sensitive values; keep it
private with the rest of the transfer files.

```bash
docker inspect dev_dataverse --format \
  '{{range .Mounts}}{{println .Source "->" .Destination}}{{end}}' \
  > "$TRANSFER_DIR/app-mounts.txt"
# Use the existing development admin authentication when prompted:
docker exec -it dev_dataverse asadmin --user admin list-jvm-options
```

Privately record any manually added options, especially
`dataverse.lang.directory`. An option added inside a container is not necessarily
persisted by copying `.properties` files or cloning the repository.
Inspect the effective file-storage directory and ensure it is included in the
backup. The compose configuration can produce `/store` when `STORAGE_DIR` is
unset. Do not assume every dataset file is under `/dv` without checking.

### Save cached images

This optional step saves the images already available locally. It does not pull
new versions of `unstable`/`latest`. The remaining services can download their
missing images normally on the new PC.

```bash
docker compose -f docker-compose-dev.yml -f docker-compose.override.yml \
  config --images | sort -u > "$TRANSFER_DIR/images-requested.txt"
: > "$TRANSFER_DIR/images-cached.txt"
while IFS= read -r image; do
  if docker image inspect "$image" >/dev/null 2>&1; then
    printf '%s\n' "$image" >> "$TRANSFER_DIR/images-cached.txt"
  fi
done < "$TRANSFER_DIR/images-requested.txt"
mapfile -t MIGRATION_IMAGES < "$TRANSFER_DIR/images-cached.txt"
if ((${#MIGRATION_IMAGES[@]})); then
  docker image save --output "$TRANSFER_DIR/docker-images.tar" \
    "${MIGRATION_IMAGES[@]}"
fi
```

Use the same Linux CPU architecture and PostgreSQL major version (currently 17)
for a physical database-directory restore. The current Solr image is 9.8.0.
If image tags were updated after the working containers were created, compare
container image IDs with tag IDs before relying on the saved image archive.

### Archive local data with services stopped

Stop only the development services that write the database, index and application
files. Confirm they are stopped before creating the archive. If they are already
stopped, skip the stop command. If the containers were removed, only their bind
mounts remain; unmounted container files cannot be recovered by this archive.

```bash
docker stop --time 60 dev_dataverse dev_solr dev_postgres
docker ps --format '{{.Names}}'
sudo tar --numeric-owner --acls --xattrs -czpf \
  "$TRANSFER_DIR/docker-dev-volumes.tar.gz" docker-dev-volumes
sudo chown "$(id -u):$(id -g)" "$TRANSFER_DIR/docker-dev-volumes.tar.gz"
chmod 0600 "$TRANSFER_DIR/docker-dev-volumes.tar.gz"
```

If the verified dataset-storage directory is outside those mounts, copy it from
the stopped container too. For example, **only if `/store` is the actual directory**:

```bash
docker cp dev_dataverse:/store "$TRANSFER_DIR/container-store"
tar -C "$TRANSFER_DIR" -czf "$TRANSFER_DIR/container-store.tar.gz" container-store
```

On the new PC, restore that additional storage with Linux permissions appropriate
to Payara and mount it at the same container path through a local compose override.
Do not silently change the database's storage-driver configuration. If unsure of
the effective storage path, confirm it before discarding the old instance.

Optional artwork archive:

```bash
tar -czf "$TRANSFER_DIR/design-assets.tar.gz" customization/homepage/assets
```

Then produce checksums and copy the transfer folder to the new PC using private
storage/SSH. Linux tar archives preserve ownership/permissions for the restore;
carry the archives as files rather than unpacking database directories in Windows.

```bash
(cd "$TRANSFER_DIR" && sha256sum *.tar* > SHA256SUMS)
# Resume the original development instance when the backup is complete:
docker start dev_postgres dev_solr dev_dataverse
```

The transfer contains local secrets and account data. Do not add it to GitHub.
Docker image archives alone do not contain bind-mounted data; see
[Docker backup guidance](https://docs.docker.com/desktop/settings-and-maintenance/backup-and-restore/).

## 2. Set up the new PC and clone

Install WSL2 Ubuntu and Docker Desktop, enable Docker's WSL integration for Ubuntu,
and install Java 21, Maven, Python 3, Git and unzip in Ubuntu. Use VS Code with WSL
if desired. Confirm Docker works from the Ubuntu shell.

```bash
java -version
mvn -version
python3 --version
docker version
docker compose version
mkdir -p ~/projects
cd ~/projects
git clone --branch codex/login-local-guide \
  git@github.com:tarkus2000ua/dataverse-uard.git dataverse
cd dataverse
```

SSH cloning requires GitHub SSH access on the new PC. HTTPS is an alternative:
`https://github.com/tarkus2000ua/dataverse-uard.git`.
The tracked `docker-compose.override.yml` already mounts branding/language files.
Since the 2026-10-22 local translation install, it also sets `JAVA_TOOL_OPTIONS`
with `-Ddataverse.lang.directory=/var/www/dataverse/lang`; keep this option when
merging local overrides. The remote Ukrainian bundle is installed under the local
`Bundle_ua.properties` filename to match the unchanged native `ua` locale.
Do not clone into `/mnt/c/` or create a Windows checkout to sync back to WSL.

## 3. Restore data before starting the new stack

Put the private transfer archives in `~/uard-transfer` on the new PC. Verify them:

```bash
cd ~/uard-transfer
sha256sum -c SHA256SUMS
# Optional, if the Docker image archive was saved:
docker image load --input docker-images.tar
```

Restore into the cloned repository **before** its first container startup. This
example refuses to merge with an existing data directory; use a separate fresh
checkout rather than overwriting an already initialized test instance.

```bash
cd ~/projects/dataverse
if [ -e docker-dev-volumes ]; then
  printf '%s\n' 'Data directory already exists; use a fresh checkout for restore.' >&2
  exit 1
fi
sudo tar --numeric-owner --acls --xattrs -xzpf \
  "$HOME/uard-transfer/docker-dev-volumes.tar.gz" -C "$PWD"
sudo test -f docker-dev-volumes/postgresql/data/PG_VERSION
# Optional artwork, if archived:
# tar -xzf "$HOME/uard-transfer/design-assets.tar.gz" -C "$PWD"
```

Keep PostgreSQL ownership and restrictive permissions from the archive. Do not
recursively chown the database to your WSL user. The startup script currently
recognizes an existing database by the directory being unreadable to that user.
Confirm that behavior before using it on restored data:

```bash
if ls docker-dev-volumes/postgresql/data >/dev/null 2>&1; then
  printf '%s\n' 'Review restored DB permissions and startup detection before starting.' >&2
  exit 1
fi
python3 scripts/customization/build-homepage.py --check
python3 scripts/customization/build-footer.py --check
./scripts/dev/dev-start-frd.sh
```

Check startup output includes **Detected existing database** and **disabling DDL
generation**. If the script describes this restored instance as first-time setup,
do not continue with fresh initialization; use an explicit restore startup
procedure that sets the generated deployment's DDL generation to `none`.
Do not run `dev-rebuild.sh`, `docker-final-setup.sh`, fresh seed scripts or the
installer to initialize the already restored database.

If extra container storage was backed up, add its mount before this startup.
Restore any manually configured Payara JVM options using the record from the old
PC. In particular, keep the language directory at the mounted path expected by
the configuration (`/var/www/dataverse/lang` if that is what the old instance used).
Use the normal development administration procedure, restart/redeploy only if an
option requires it, and retain existing settings restored in PostgreSQL.

## 4. Verify and continue development

```bash
curl --fail-with-body http://localhost:8080/api/info/version
curl --fail-with-body http://localhost:8080/api/dataverses/:root/featured
```

Open http://localhost:8080 and verify your old account login, featured institution
logos, collection/dataset inventory, a dataset download, UA/EN switching, article
links, `/guide/` chapter links and the local support dialog. Native top-menu About
and User Guide should retain the restored `:NavbarAboutUrl` and
`:NavbarGuidesUrl` settings. The existing `localhost:8080` settings still apply when
you use that same local port on the new PC.

The current taxonomy-card links target the remote alias `/dataverse/uard` and its
custom fields. Our local root/metadata differ; those filters are not evidence that
local data restoration failed. See [homepage navigation](homepage-navigation.md).

For source/resource changes use `./scripts/dev/dev-frd.sh`; mounted branding CSS
and generated HTML refresh normally. Read [README_HANDOFF.md](../README_HANDOFF.md)
and [DEV_SETUP.md](../DEV_SETUP.md) before the next change.

## After the migrated-environment fixes (2026-11-01)

The current branch also versions the installed Ukrainian baseline, the guide
builder/generated pages, the unchanged Dataverse logo wordmark, and the restored
ORCID documentation link. Guide menu settings move with the database; if absent
on a new local setup, configure the native hook for `http://localhost:8080/guide/`
after deploying the generated files. Preserve the trailing slash.

Read [FRD troubleshooting](frd-troubleshooting.md) for stale WSL bind mounts.
The full startup script refreshes only the application container after extracting
the WAR; quick FRD checks the bind mount and propagates deployment errors.
Do not reconnect/recreate a database automatically because an application mount
is stale. Existing runtime data and cached images still move privately, not in Git.
