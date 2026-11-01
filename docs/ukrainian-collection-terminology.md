# UaRD: Ukrainian collection terminology — remote update guide

Prepared 2026-11-01 for Dataverse 6.12. This update changes 275 Ukrainian translation entries and localizes one My Data icon tooltip. Action labels use verbs: «Додати», «Видалити», «Редагувати», «Опублікувати», «Пов’язати». Content containers are «колекції», nested containers «підколекції», and Host Dataverse is «Батьківська колекція». Reviewed account wording uses «обліковий запис репозитарію».

This package has not been installed on the remote server.

## Package contents

- **translation-report.html** — the nontechnical English / current Ukrainian / new Ukrainian table. Open it in a browser; search or print it.
- **language/Bundle_ua.properties** and **language/Bundle_uk.properties** — identical complete updated Ukrainian bundles. Use only the filename already used by the server.
- **merge/apply-bundle-update.py** and **merge/uk-collection-terminology.json** — merge only the reviewed entries into a downloaded remote bundle, preserving other server translations. Python 3 is needed on the computer doing the merge.
- **application/** — three files for the My Data tooltip, with paths relative to the deployed application.
- **mydata-tooltip.patch** — the same three small source changes as a patch.
- **SHA256SUMS** — package file checksums.
- **VALIDATION.md** — completed local checks and limitations.

Dataverse platform attribution, English translations, API paths, object identifiers, stored names such as Root, authentication configuration and actual permissions are unchanged. Some notification/email text and human-readable collection error messages use the same Ukrainian bundle and receive the terminology too.

## 1. Confirm two remote locations

The server is reported to use direct Payara. Use the existing SFTP/administration account. Ask the administrator to confirm:

1. The active external language directory configured by the JVM property **dataverse.lang.directory**, and the Ukrainian filename used there: **Bundle_uk.properties** or **Bundle_ua.properties**.
2. The active deployed Dataverse application directory, containing **dataverse_template.xhtml** and **WEB-INF**.

Keep the server's working locale and filename. Do not add another Ukrainian option or assume local development paths also apply remotely. These files do not go in the external homepage/footer branding directory.

If you cannot access these locations or reload the application, give the package and this guide to the administrator. No production access is assumed.

## 2. Download and keep the originals

Before replacing anything, download the current Ukrainian bundle and these three active application files:

- **mydata_fragment.xhtml**
- **mydata_templates/cards_minimum.html**
- **resources/js/mydata.js**

Keep the dated backup separately from the upload files. Retain the maintained application/WAR so later redeployments keep the tooltip update.

This is a translation/presentation update. Do not run the installer, seed scripts, database imports, Solr resets, Docker volume recreation or development startup scripts on the remote server.

## 3. Prepare the Ukrainian bundle

### Merge into the current remote bundle

This is the recommended route because it preserves translations maintained only on the server.

Extract the ZIP into a working directory. Put the downloaded remote Ukrainian bundle there, under its existing name. Open that directory in WSL/Linux and run the following. Substitute **Bundle_ua.properties** if the server uses **ua**:

~~~bash
python3 merge/apply-bundle-update.py Bundle_uk.properties \
  --patch merge/uk-collection-terminology.json --check
~~~

The check writes nothing. A matching baseline reports 275 entries to update; entries already updated are accepted too.

If it reports a conflict, do not overwrite the server bundle with a complete local file. An affected entry is missing, duplicated, continued across lines, or has different wording. Send the conflict list and downloaded bundle for a reviewed merge. No output or input file is modified on conflicts.

When the check passes, create a separate output file:

~~~bash
python3 merge/apply-bundle-update.py Bundle_uk.properties \
  --patch merge/uk-collection-terminology.json \
  --output Bundle_uk.updated.properties
~~~

The helper never overwrites its input or an existing output. Upload the resulting file using the server's original filename, **Bundle_uk.properties** in this example. The **.updated** suffix is for local preparation only.

### Complete-file option

The **language/** directory contains ready-to-upload complete Ukrainian files. Use one only after confirming that replacing the whole remote bundle will not lose remote-only translations. These files include the existing local Ukrainian baseline and the earlier ORCID wording/apostrophe correction.

The merge route is preferable when the current remote bundle is available. Its output may differ from the supplied complete file because it preserves extra remote translations. No English bundle needs replacing.

## 4. Install the My Data tooltip update

Upload/merge the three files from **application/** into the confirmed active application, preserving the relative directories.

If the remote files have their own customizations, merge the small changes rather than replacing whole files:

1. **mydata_fragment.xhtml** adds the translated data attribute to the existing resultsTable element.
2. **resources/js/mydata.js** passes that label into the existing Nunjucks card-rendering context.
3. **mydata_templates/cards_minimum.html** uses the label instead of the literal Dataverse icon title.

The supplied patch shows the exact changes. An administrator can check it from the active application root:

~~~bash
patch --dry-run -p4 < /path/to/mydata-tooltip.patch
~~~

Only after a successful dry run and diff review:

~~~bash
patch -p4 < /path/to/mydata-tooltip.patch
~~~

The strip level matches the supplied a/src/main/webapp/... paths. Keep all three changes together. Native locale selection and Nunjucks autoescaping remain in use.

For a packaged WAR deployment, include the same three changes in the maintained remote build and use the normal deployment process. Do not upload an old local development WAR. If the tooltip update is deferred, the bundle update still works independently, but this one tooltip retains “Dataverse”.

## 5. Reload the application and refresh the browser

Ask the administrator to reload/redeploy the existing Dataverse application through the established Payara procedure to clear translation caches. If that installation requires restarting the existing Payara service/domain to reload external bundles, use its normal restart procedure in an agreed maintenance window.

External-bundle updates need no Maven rebuild. The tooltip files need application deployment as described above. Keep them in the maintained deployment artifact so future releases retain the change.

Preserve file ownership/permissions and confirm Payara can read the files. Do not change language settings, the JVM language-directory option, authentication providers, credentials or database settings.

After the reload, hard-refresh the browser with Ctrl+Shift+R to fetch the updated JavaScript/template. A browser refresh alone may not clear server-side bundle caches.

## 6. Verify on the public server

Use a fresh Ukrainian session:

1. **Додати дані** should offer **Нова колекція**.
2. A collection/dataset creation form should show **Батьківська колекція** and revised help text. Close it without saving.
3. The collection Edit menu should show **Вибрані колекції**.
4. With an existing authorized account, inspect Permissions and Edit Access. Expect **Хто може додавати дані до цієї колекції?** and the four revised options. Do not save access changes merely to test wording.
5. Check search, Advanced Search, linking dialogs, templates and My Data. The collection icon tooltip in My Data should say **Колекція**.
6. Switch Ukrainian → English → Ukrainian using the native selector. English retains its existing Dataverse wording.
7. Confirm names, links, login, available actions and actual permissions behave as before. Look for missing-key messages or literal placeholders such as {0}.
8. Inspect captured test notifications/email only if an approved test-mail environment exists; do not send test email to real users solely for this verification.

The table's {0}, {1} and similar markers represent names, addresses or values inserted by the application. They should not appear literally in formatted live messages.

## Rollback

Restore the backed-up Ukrainian bundle and the three backed-up application files, then reload the existing application and hard-refresh the browser. If only the bundle update was installed, restore only that bundle. Preserve unrelated changes made since the backup.

No database rollback, import, Solr reset or volume deletion is required.

## Repository maintenance

The maintained sources are the Ukrainian bundle, the manifest at **customization/translations/uk-collection-terminology.json**, the helper at **scripts/customization/apply-bundle-update.py**, its tests, the three My Data files, **docs/reports/ukrainian-collection-terminology.html**, and **docs/upstream-modifications.md**.

Local source changes use the required **./scripts/dev/dev-frd.sh** workflow. The packaged **VALIDATION.md** records what was actually checked and any authenticated/browser limitations.
