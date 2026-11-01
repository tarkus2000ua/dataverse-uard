#!/usr/bin/env bash
set -euo pipefail

# Ensure we're in project root
cd "$(dirname "${BASH_SOURCE[0]}")/../.."

echo "Building Dataverse WAR for fast redeploy..."
mvn -T 1C -DskipTests -DskipUnitTests -DskipIntegrationTests clean package

echo "Extracting WAR into target/dataverse/..."
mkdir -p target/dataverse
unzip -oq target/dataverse-*.war -d target/dataverse/

# Check if database is already initialized (before creating directories)
# If postgres has initialized, the data dir will have restrictive permissions (0700)
# On first run, the directory either doesn't exist or has default permissions
DB_INITIALIZED=false
if [ -d "docker-dev-volumes/postgresql/data" ]; then
    # Try to list the directory - if permission denied, it means postgres owns it (initialized)
    if ! ls docker-dev-volumes/postgresql/data >/dev/null 2>&1; then
        DB_INITIALIZED=true
    fi
fi

echo "Ensuring docker volume mount points exist..."
mkdir -p docker-dev-volumes/app/data
mkdir -p docker-dev-volumes/app/secrets
mkdir -p docker-dev-volumes/postgresql/data
mkdir -p docker-dev-volumes/solr/data
mkdir -p docker-dev-volumes/solr/conf

# Only disable DDL generation if database is already initialized
# (on first run, we need create-tables to bootstrap the schema)
if [ "$DB_INITIALIZED" = true ]; then
    echo "Detected existing database - disabling DDL generation to preserve schema..."
    sed -i.bak 's/\(eclipselink.ddl-generation" value="\)create-tables/\1none/' \
        target/dataverse/WEB-INF/classes/META-INF/persistence.xml
    rm -f target/dataverse/WEB-INF/classes/META-INF/persistence.xml.bak
else
    echo "First-time setup detected - keeping DDL generation enabled for schema creation..."
fi

echo "Starting dev stack (SKIP_DEPLOY=1)..."
export SKIP_DEPLOY=1
# Use override file if it exists (for local customizations like memory limits)
compose=(docker compose -f docker-compose-dev.yml)
if [ -f docker-compose.override.yml ]; then
    compose+=(-f docker-compose.override.yml)
fi
"${compose[@]}" up -d

# Maven clean replaces target/dataverse. An existing bind mount can still point
# at the removed directory, so recreate only the application container afterward.
echo "Refreshing the application bind mounts..."
"${compose[@]}" up -d --no-deps --force-recreate dev_dataverse

echo "Waiting for Payara to be ready..."
payara_ready=false
for ((attempt = 0; attempt < 90; attempt++)); do
    if curl -sf --max-time 5 http://localhost:8080/ >/dev/null 2>&1; then
        payara_ready=true
        break
    fi
    sleep 2
done
if [ "$payara_ready" != true ]; then
    echo "Error: Payara did not become ready. Inspect docker logs dev_dataverse." >&2
    exit 1
fi

echo "Deploying exploded WAR..."
./scripts/dev/dev-deploy-frd.sh

echo ""
echo "✓ Fast redeploy environment ready!"
echo "  Next: Make code changes, then run './scripts/dev/dev-frd.sh' to redeploy (~12s)"
