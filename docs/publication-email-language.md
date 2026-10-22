# Publication email language

Updated 2026-10-22. This is a source change on `codex/publication-email-locale`;
remote deployment is not performed by this task.

## Behavior

Dataset publication finishes asynchronously, where the initiating JSF view is no
longer available. Previously the email subject, body and signature resolved their
language independently from the worker environment, so publication from a Ukrainian
UI could generate an English email.

The native `DataverseSession.localeCode` is now captured as an immutable optional
notification locale when the JSF `DataverseRequest` is created. The same request
already travels through the publication commands into the background worker.
Finalization passes this locale to publication-success, PID-publication-failure,
and file-access notifications created during publication. A single resource bundle
is used for their email subject, message strings, greeting and closing/support label.
No global JVM locale or thread-local preference is changed.

Native `ua`, standard `uk`, and English `en` remain distinct bundle codes. The fix
does not rename bundles or language settings and does not translate stored dataset
or collection names. It uses whatever maintained translation files are configured
in that installation. A translated key that is missing still follows Java's
existing resource-bundle fallback behavior.

API/internal requests created without a notification locale keep the existing
environment-based fallback. This is the publisher's chosen UI language, not a new
per-recipient language preference. Other independently triggered emails retain
their existing language-selection behavior.

## Waiting workflows

The in-memory workflow context already retains its `DataverseRequest`. A pending
workflow stores the optional locale in its existing `localData` map using the
reserved `:dataverse.notificationLocale` key and restores it with the request when
resuming. This internal key is excluded from the data passed back to the workflow
step. No new database column or schema migration is needed. Existing pending
workflows without the key retain the previous fallback behavior. Workflow providers
must not use this reserved key in their own pending-step data.

## Source changes

- `DataverseRequestServiceBean` and `engine/command/DataverseRequest`: capture and carry the native UI language.
- `workflow/PendingWorkflowInvocation`: retain it across a persisted workflow pause.
- `engine/command/impl/FinalizeDatasetPublicationCommand`: pass it to publication notifications.
- `UserNotificationServiceBean`: add explicit-locale entry points while retaining existing callers and mute rules.
- `MailServiceBean`, `util/MailUtil`, and `branding/BrandingUtil`: use one explicit bundle for email strings and the closing support label.

Existing authorization, publication transactions, recipient selection, email and
in-app notification preferences are preserved. No branding fragments, generated
JavaScript, language files, SMTP settings, or featured collections are changed.

## Verification

The targeted tests capture actual generated MIME messages in a worker thread and
intercept `Transport.send`; no real SMTP delivery occurs. They cover `uk`, legacy
`ua`, English, plain text and HTML closing text, concurrent English/Ukrainian
messages, publication failure, granted file access, request-locale snapshots,
workflow restoration, old workflows, API fallback, and forwarding from the
publication finalization command. Existing mail, bundle, branding and notification
unit tests are included in the focused run.

```bash
mvn -Dtest=DataverseRequestServiceBeanTest,PendingWorkflowInvocationLocaleTest,PublicationEmailLocaleTest,PublicationNotificationLocaleTest,MailServiceBeanTest,MailUtilTest,BundleUtilTest,BrandingUtilTest,UserNotificationServiceBeanTest -DskipIntegrationTests test
git diff --check
```

Focused Maven run on 2026-10-22: BUILD SUCCESS; 79 tests, zero failures/errors/skips.
Application and test sources compile; forbidden-API checks and `git diff --check`
pass. Real SMTP is intercepted in tests.

The changed application was deployed locally with `./scripts/dev/dev-frd.sh`.
The user confirmed successful creation/publication scenarios in English and
Ukrainian with the local MailDev test mailbox on 2026-10-22. The Ukrainian run used
the separately supplied server translation, installed locally without editing its
contents. That local translation installation and Docker configuration are not
part of this code fix. Live verification of a configured pre-publication workflow
is still pending; request restoration is covered by the unit tests above.

For future local verification, use the existing restored stack, FRD and a local
test mailbox. Do not initialize a new database. Confirm the installed bundles and
native JSF selection; do not send test email to real recipients.

## Deployment and rollback

This is a Java application change and requires the site's normal rebuilt WAR or
equivalent application deployment, not replacement of translation files alone.
Reconcile with the actual remote `master-9769107` source and server-specific changes
before preparing a production artifact. Preserve maintained Ukrainian bundles,
language settings, storage, accounts, and application settings.

Deploy only when explicitly requested. Retain the previous application artifact
for rollback. No locale/environment change is required by this implementation.
If rolling back while workflows are pending, the previous application may expose
the reserved map entry to a resumed custom workflow step; review those pending
workflows through the site's established procedure before rollback.
