# Fast-redeploy bind mounts and deployment errors

## Recovery verified on 2026-10-25

Payara's `Archive type of /opt/payara/deployments/dataverse was not recognized`
can mean that its bind mount is empty, even though `target/dataverse/WEB-INF/web.xml`
exists in WSL. Inspect both sides before rebuilding or changing application code.
Maven `clean` replaces `target/dataverse`; a running container may retain a bind
mount to the removed directory. Other stale WSL mounts can affect branding,
language files, or database data independently of that Maven directory replacement.

On this occasion, fresh read-only mounts could see all existing WSL files while
the running application container could not. PostgreSQL was also attached to a
different, newly initialized directory: its database had zero public tables and
its PG_VERSION was created on October 25. The existing WSL database directory had
PG_VERSION from October 3. Its restored database has 121 public tables, the six
featured collections and the prior application settings.

The empty running database was dumped before recovery. Only `dev_postgres` and
`dev_dataverse` were recreated, using their exact existing cached images and
existing WSL bind directories. Solr and the other services were not recreated.
No database seed, schema creation, volume deletion, or remote operation was run.
The exploded deployment retained `eclipselink.ddl-generation=none`.

Do not automatically recreate PostgreSQL for every application build. If a
database mount mismatch recurs, inspect the data in both locations, preserve any
new running-container data, and verify image/PostgreSQL-major compatibility
before reconnecting the existing directory. Do not use `docker compose down -v`.

## Script behavior

`scripts/dev/dev-start-frd.sh` now recreates only the application container after
the build and normal stack startup, so it sees the newly extracted directory.
Payara readiness polling is bounded and reports a failure instead of waiting
indefinitely. Existing database detection and DDL handling are retained.

Both startup and quick redeploy call `scripts/dev/dev-deploy-frd.sh`, which checks
for WEB-INF/web.xml on the host and through the application bind mount. It runs
asadmin with shell error and pipeline failure propagation. A deployment failure
returns a nonzero status to the caller, so neither wrapper prints its success
banner after a failure. The temporary admin password file is removed on success
or failure. Existing local development authentication is unchanged.

From the WSL repository:

```bash
# Full build/start, including application mount refresh:
./scripts/dev/dev-start-frd.sh
# Subsequent source/resource changes:
./scripts/dev/dev-frd.sh
# Isolated shell-control regression tests, without Docker/Maven or DB changes:
python3 scripts/dev/tests/test_frd.py
```

Verified: asadmin deployment succeeded, /api/info/version reports 6.12, the six
featured aliases and their existing logos are available, and the native browse
page displays the existing collections and two datasets. The new homepage button
opens /dataverse.xhtml. Native UA -> EN -> UA switching changes its label, and
desktop/mobile homepage checks show no horizontal overflow. Regression tests
cover successful deployment, error propagation to both wrappers, temporary-file
cleanup and an unavailable bind mount. Git whitespace and branding checks pass.
