#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "${BASH_SOURCE[0]}")/../.."

if [ ! -s target/dataverse/WEB-INF/web.xml ]; then
    echo "Error: target/dataverse is not an exploded Dataverse WAR. Run ./scripts/dev/dev-start-frd.sh." >&2
    exit 1
fi

if ! docker exec dev_dataverse test -s /opt/payara/deployments/dataverse/WEB-INF/web.xml; then
    echo "Error: dev_dataverse cannot see the exploded WAR through its bind mount." >&2
    echo "Run ./scripts/dev/dev-start-frd.sh to recreate the application container after the build." >&2
    exit 1
fi

docker exec dev_dataverse /bin/bash -euo pipefail -c '
    pwdfile=$(mktemp)
    trap "rm -f -- \"$pwdfile\"" EXIT
    printf "AS_ADMIN_PASSWORD=%s\n" admin > "$pwdfile"
    asadmin --user admin --passwordfile "$pwdfile" \
        deploy --force --upload=false /opt/payara/deployments/dataverse 2>&1 \
        | sed -e "/PER01001/d" -e "/PER01003/d" -e "/Command deploy completed with warnings/d"
'
