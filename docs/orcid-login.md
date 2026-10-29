# UaRD ORCID login presentation (2026-10-29)

Target: Dataverse 6.12. This change customizes presentation for the already
configured ORCID provider; it does not install or enable an authentication provider.

## Components and resources

- `src/main/webapp/loginpage.xhtml`: native login view, heading, OAuth introduction,
  support command, ORCID logo/button and the former two long ORCID paragraphs.
- `src/main/java/propertyFiles/Bundle.properties`: packaged English fallback.
- `customization/lang/Bundle.properties`: existing external English base bundle.
- `customization/lang/Bundle_ua.properties`: existing external Ukrainian bundle,
  using the local legacy locale code `ua`. The server may use `uk`; retain its
  current bundle filename and locale configuration.
- `LoginPage.java`: selects the enabled provider and carries `redirectPage`.
- `authorization/providers/oauth2/OAuth2LoginBackingBean.java`: named `OAuth2Page`,
  supplies the existing `linkFor(providerId, redirectPage)` OAuth entry URL.
- `authorization/providers/oauth2/impl/OrcidOAuth2AP.java`: existing ORCID provider.
  These Java classes are unchanged.

The heading still uses `login=Увійти`. The native button still uses
`login.button.orcid=Створіть або підключіть ваш ORCID` and the existing ORCID logo.
Only providers matching the native ORCID ID condition receive the new
`org.login.orcid.blurb` string:

> Увійдіть або створіть обліковий запис UaRD за допомогою ORCID. Виникли проблеми? Зверніться до [служби підтримки] по допомогу.

The bracketed phrase above represents the native support link. Its actual label
comes from `settingsWrapper.supportTeamName`; it is not hardcoded by this patch.
The two lengthy `auth.providers.orcid.helpmessage1/2` paragraphs are removed from
this view; their bundle values remain untouched. Other OAuth providers retain
`auth.providers.blurb`, and builtin/Shibboleth UI and provider-selection rules remain.

## Literal placeholder correction

`h:outputFormat` uses Java `MessageFormat`. The Ukrainian `auth.providers.blurb`
contained `зв'яжіться з {3}`. Its single ASCII apostrophe starts a quoted section,
so the support placeholder prints literally. The targeted correction is
`зв''яжіться з {3}` (two ASCII apostrophes); rendered Ukrainian retains one apostrophe.
No other existing Ukrainian translation key is changed by this task.

The new ORCID string retains `{3}` for the support component. All four existing
`o:param` children stay in the same order: provider title, guide base, guide version,
and support command. The concise ORCID copy only uses the final parameter.
Keep the doubled-apostrophe rule for future text used with `h:outputFormat`.

## Deployment on the ORCID-enabled test server

No remote deployment was performed. Back up the active login view and the affected
bundle files. Merge with the server's existing Dataverse 6.12 customizations.

1. Deploy the updated `loginpage.xhtml` beside `dataverse_template.xhtml` in the
   confirmed active application. Preserve the server's other view changes. Keep
   the change in the maintained deployment artifact; packaged deployments use the
   normal rebuilt WAR. An old WAR in local `target/` is not rebuilt by FRD.
2. Merge `org.login.orcid.blurb` into the active external base `Bundle.properties`
   and the active Ukrainian bundle (`Bundle_uk.properties` or `Bundle_ua.properties`).
   The incremental package provides small UTF-8 snippets, not replacement bundles.
   Updating only the packaged English fallback is insufficient when external
   bundles are configured. Preserve all other maintained translations.
3. In that Ukrainian bundle, correct the apostrophe in the existing
   `auth.providers.blurb` as described above. Preserve any server-specific wording.
   The package includes the local corrected value as a reference.
4. Reload/redeploy the application through the site's existing procedure to load
   XHTML and clear resource-bundle caches. A browser refresh alone may not clear
   server-side bundle caches. Verify file paths and readability by Payara.

Do not change OAuth credentials, provider definitions/enabled state,
`:DefaultAuthProvider`, `:AllowSignUp`, language/JVM settings or database data.
No branding CSS, homepage/footer or OAuth Java changes are required.
The support action, `OAuth2Page.linkFor(LoginPage.authProvider.id,
LoginPage.redirectPage)`, provider rendering and all redirect metadata are unchanged.

Rollback: restore the backed-up login view and only the affected bundle entries,
then reload through the same procedure. No database rollback is needed.

## Checks performed locally

- Required `./scripts/dev/dev-frd.sh`: compilation and Payara deployment succeeded.
  This helper skips Java test suites; no full unit/integration suite was run.
- Live local `/api/info/version` reports 6.12. After redeployment,
  `/loginpage.xhtml` returns HTTP 200 with `Увійти` and the existing builtin form.
- Isolated Java 21 `MessageFormat` check reproduces the old literal `{3}`, confirms
  the apostrophe fix and formats the new English/Ukrainian text with a support-link
  parameter and no unresolved placeholders.
- Actual Jakarta EL evaluation of the view's expression selects the concise text
  for `orcid-public`/`orcid-sandbox` and the standard text for Google/GitHub in both
  English and Ukrainian. This uses fixtures and does not enable any provider.
- XHTML XML parsing, exact heading/button values, all four format parameters,
  OAuth/support/form/metadata preservation and targeted bundle-key diffs pass.
- Both branding builders' `--check` options and `git diff --check` pass.
- Saved hashes confirm the original Git index and unrelated dirty/untracked files
  are unchanged. The large pre-existing Ukrainian bundle replacement is preserved;
  this task adds one key and fixes only the existing OAuth blurb's apostrophe.

Local ORCID is unconfigured. These checks do not establish a successful ORCID
authorization/callback or the final ORCID-only layout on the test server.
No authentication settings were changed for local testing.

## Acceptance on the enabled test server

1. Open `/loginpage.xhtml` signed out in a fresh private Ukrainian session. Expect
   `Увійти`, the short UaRD explanation, a functional support link and exactly one
   `Створіть або підключіть ваш ORCID` button with its logo. The long ORCID
   descriptions, literal `{3}` and missing-key markers must be absent.
2. Open/close support without submitting a message. Confirm the actual configured
   support label and native dialog behavior. Check keyboard Tab/Enter, visible
   focus and no horizontal overflow at desktop and 375/320px mobile widths.
3. Switch Ukrainian -> English -> Ukrainian through the native language selector;
   verify concise account text, native labels and session language persistence.
4. From a page that requires login (or a known safe `redirectPage` destination),
   click ORCID and confirm the same configured ORCID authorization endpoint and
   callback/return destination as before the patch. Do not record/share OAuth
   state, authorization codes, tokens or credentials.
5. With an authorized existing test account, complete ORCID sign-in and verify the
   expected return page and user menu. Cancel a second attempt and verify the
   normal error/cancel path. If a disposable new ORCID-linked account is approved
   for this test server, check the normal first-login account-creation flow too.
   The copy does not override the server's account-creation policy.
6. Confirm builtin login/signup controls remain absent under the server's existing
   ORCID-only settings, and inspect Payara logs for view/bundle errors.

## Complete replacement package requested 2026-10-29

The user subsequently requested fully prepared local files for SFTP replacement,
instead of manually merging the earlier translation snippets. Local source,
active deployed XHTML and mounted bundles were confirmed updated. A complete-file
package is prepared from `src/main/webapp/loginpage.xhtml`,
`customization/lang/Bundle.properties` and `customization/lang/Bundle_ua.properties`.
The Ukrainian output is also named `Bundle_uk.properties` for a server using `uk`;
both names have identical contents, and only the server's existing name is used.
No locale/provider configuration is changed.

This package preserves the user-supplied Ukrainian server translation installed
locally on 2026-10-22 (pre-task SHA-256
`5dac5203bf725222bd38cffaaee340f2c206ce5f851ba1c376077b3b3bcb35d1`).
It includes only the targeted apostrophe correction and new ORCID key on that
Ukrainian baseline; the English file uses the existing local base. Complete-file
replacement cannot preserve later remote-only edits that are absent locally.
Download the three current remote files as backups and retain any such edits.
Install the two bundles and matching view together, reload through the existing
Payara procedure and follow the acceptance steps above. No manual key editing
is needed when the remote baseline matches these prepared complete files.
The earlier incremental-snippet instructions remain an alternative for bundles
that have diverged. No remote deployment was performed.

## Git scope

The ORCID feature commit includes only its new bundle key and apostrophe correction
in the tracked Ukrainian baseline. The larger, pre-existing replacement with the
supplied server translation remains local pending work and is excluded from this
feature commit. The prepared full upload bundle retains that complete server
translation. Unrelated staged and untracked work remains outside this commit.
