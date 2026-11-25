# Ukrainian Geospatial metadata

The Geospatial block uses its own `geospatial` resource bundle. Its field labels,
help text, input hints and controlled vocabulary labels do not come from
`Bundle_ua.properties` or `Bundle_uk.properties`.

## Maintained resources

- `customization/lang/geospatial_uk.properties`: standard Ukrainian locale used
  on the server.
- `customization/lang/geospatial_ua.properties`: identical resource for the
  existing local legacy `ua` locale. Keep the two files byte-identical.
- `customization/translations/uk-geospatial.json`: the 103 translated values,
  suitable for the existing `scripts/customization/apply-bundle-update.py` helper.

The baseline is the external Ukrainian bundle captured on November 15, 2026.
Its SHA-256 was
`7525c20c2589c822b02fea3027d02c61a3c467a2ddfe8c006306d44d29503a38`.
Existing Ukrainian labels and country names are preserved. The key set matches
the Dataverse 6.12 packaged English bundle: 408 keys, including 38 field
definitions (16 top-level fields and 22 subfields).

The update translates the remaining 25 human-readable field/subfield titles,
27 descriptions (including the already-localized resource-type field's help),
nine input hints and 42 controlled vocabulary labels. `URL`, the ISO date hint
`yyyy-mm-dd`, protocol identifiers and unit symbols retain their technical forms.
Only display labels change; field names, stored vocabulary values, metadata
schemas and English resources are unchanged.

## Local use

The existing compose override mounts `customization/lang` at
`/var/www/dataverse/lang`; `dataverse.lang.directory` points there. Use the file
matching the existing Dataverse locale. Both Ukrainian files are supplied so no
locale setting needs changing.

External bundle changes do not require a Maven build. An already-running Payara
application can retain cached bundles; use the existing local
`./scripts/dev/dev-deploy-frd.sh` helper against the running exploded application
to reload them. A browser refresh alone does not clear the Java bundle cache.
An isolated Git worktree is not automatically the running container's bind mount.

## Preserve server-only translations during installation

Download the active `geospatial_uk.properties` (or the installation's existing
`geospatial_ua.properties`) before preparing an update:

```bash
python3 scripts/customization/apply-bundle-update.py /path/to/geospatial_uk.properties \
  --patch customization/translations/uk-geospatial.json --check
python3 scripts/customization/apply-bundle-update.py /path/to/geospatial_uk.properties \
  --patch customization/translations/uk-geospatial.json \
  --output /path/to/geospatial_uk.updated.properties
```

The helper preserves unrelated entries and rejects missing keys, duplicates or
conflicting translations. Review any conflicts rather than overwriting them.
Install the result under the original active filename, retaining its owner,
group and mode. Retain the English bundle and existing locale/JVM settings.
Reload the installation's Payara application/service to clear cached bundles.
No metadata-block reload or database change is needed.

## Verification

Check UTF-8 parsing with Java properties/resource-bundle loading, exact key-set
parity with Dataverse 6.12, identical `uk`/`ua` contents and preservation of all
pre-existing translations. Both Ukrainian locales must resolve all labels;
English must still resolve the packaged English values.

For UI acceptance, open an editable draft in a collection with Geospatial
enabled. In Ukrainian, inspect all 16 top-level fields, 22 subfields, help text,
input hints and dropdown options. Save representative compound/vocabulary
metadata, reopen the draft, and switch English → Ukrainian. Technical formats
and stored metadata must retain their normal behavior. Enabling Geospatial on
collections is a separate configuration task (BUG-004).

## Confirmed deployments (2026-11-25)

### Local Dataverse

Installed both Ukrainian files in the existing checkout's active language mount
at `/home/andrey/projects/dataverse/customization/lang`. The four pre-existing
language files were retained unchanged. Reloaded the existing exploded application
with `scripts/dev/dev-deploy-frd.sh`; no Maven build or container recreation was
needed. The API reports Dataverse 6.12.

An isolated Java probe using the running container's application classes and
language mount resolved all 408 keys correctly for `ua`, `uk` and English.
Native JSF English/Ukrainian switching also passed. None of the ten publicly
visible local collections enables Geospatial, so its edit form was not exercised.
The deployment/checksum records are under
`/home/andrey/projects/dataverse/work/geospatial-local-deploy-20261125T132959/`.

### uard2

Deployed through `designer@212.111.197.42:20124`. Confirmed that the active
Payara process uses `-Ddataverse.lang.directory=/dv/langBundles`. The server's
existing `uk` locale was retained.

The conflict-checked manifest updated only
`/dv/langBundles/geospatial_uk.properties`, preserving the original `root:root`
ownership and `0644` mode. All other 64 language files, including the English
Geospatial bundle, retain their pre-deployment checksums and metadata.
The installed Ukrainian bundle's SHA-256 is
`a44bd474e1ac2d036121e109fbb0fbe4655829420c1089d618dea2b390c9eb5a`.

Release and backup directory:
`/home/designer/uard-releases/geospatial-20261125T113612Z/`.
It contains `before-langBundles.tar`, the original and updated Ukrainian bundles,
the manifest/helper, and `before.json`, `prepared.json`, `installed.json` and
`verified.json` audit records.

Restarted only `payara_domain1.service` to clear bundle caches. Post-restart
checks confirmed the active service, all 408 keys and all 103 translated values,
unchanged language-file ownership/modes, and a native JSF English → Ukrainian
round trip. The public HTTPS version endpoint reports Dataverse 6.12. The
authenticated dataset edit form was not exercised.

For a targeted rollback, restore only `langBundles/geospatial_uk.properties` from
the retained archive into `/dv`, preserving its recorded ownership/mode, then
restart the same Payara service and repeat the language/version checks. No WAR,
metadata schema, database setting or collection configuration was changed.
