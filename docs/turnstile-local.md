# Local Turnstile integration for UaRD

Updated 2026-11-05. Dataverse 6.12. Integrated into `custom/6.12`; original
work branch `codex/contact-form` is retired. Implementation commit: `e5e1424bbf`.

## What is implemented

The shared native contact dialog selects arithmetic (default) or Turnstile through
server configuration. This applies to support, collection and dataset contact
contexts; recipients, authenticated sender handling, optional CC, native JSF
validation and mail composition are retained.

Turnstile is explicitly rendered when the dialog opens. The integration handles
dialog replacement, form AJAX validation updates, retry, expiration, cancellation
and responsive compact/flexible widgets. Native ua maps to widget language uk.
The current UaRD stylesheet also styles the verification panel.

Dataverse's dv_rebind_bootstrap_ui.js replaces PrimeFaces Dialog.postShow without
calling the usual onShow callback. The new script observes dialog visibility and
content replacement instead of modifying that native resize implementation.
The script URL has a content-version query; update it when editing the script.

Before mail can be sent, SendFeedbackDialog verifies the challenge on the server.
TurnstileVerifier calls the fixed HTTPS Siteverify endpoint with a 3-second connect
timeout and an 8-second request timeout, rejects redirects, limits accepted response
size and checks success, configured hostname and action=contact_form. Missing,
rejected, expired/reused or unavailable verification prevents sending and marks
JSF validation failed. The token is cleared after a verification attempt.

Arithmetic also has an action-level check so bypassing its JSF field cannot skip
the challenge. An invalid/unknown Turnstile configuration never falls back to math.
Only an explicit provider change selects arithmetic.

## Configuration

These are new MicroProfile/JvmSettings properties, not existing database settings:

| Property | Default / purpose |
| --- | --- |
| dataverse.contact.captcha-provider | math; set turnstile to select the new provider |
| dataverse.contact.turnstile.site-key | Public widget key |
| dataverse.contact.turnstile.secret-key | Server-only verification secret |
| dataverse.contact.turnstile.hostname | Expected Cloudflare verification hostname, without scheme/port |
| dataverse.contact.turnstile.test-mode | false; explicit loopback-only dummy-key mode |

Use the site's protected Payara/MicroProfile configuration mechanism.
Environment equivalents follow Dataverse's normal MicroProfile naming, e.g.
DATAVERSE_CONTACT_TURNSTILE_SECRET_KEY. Keep real secrets outside Git, browser
markup and shared logs. The private secret is never exposed by the view.

## Current local configuration

The existing local Payara domain has these contact-only system properties:

```properties
dataverse.contact.captcha-provider=turnstile
dataverse.contact.turnstile.site-key=1x00000000000000000000AA
dataverse.contact.turnstile.secret-key=1x0000000000000000000000000000000AA
dataverse.contact.turnstile.hostname=localhost
dataverse.contact.turnstile.test-mode=true
```

Both keys above are Cloudflare's publicly documented dummy credentials, not
production secrets. The widget displays Cloudflare's test-only banner.

Use http://localhost:8080. The configured expected host and the actual request
host must both be loopback in test mode, and both keys must be official dummy
keys. Real mode rejects dummy keys. Official dummy responses carry
metadata.result_with_testing_key=true and can use example.com without action;
that exception is accepted only in explicit local test mode. Real mode always
checks the configured hostname and contact_form action.

The prior contact-only domain properties were absent; their private snapshot is
/tmp/uard-contact-config-before-shqe4l6r.json in WSL. No database, authentication,
SMTP or provider settings were changed. No containers were recreated or restarted.

System properties set through asadmin are in the existing container's Payara domain.
They persist through application FRD, but are not guaranteed to survive container
recreation. Compose was not changed. To preserve configuration across recreation,
set the same values through the established persistent environment/JVM mechanism,
retaining dataverse.lang.directory and all unrelated options.

To return to arithmetic through the existing local admin procedure:

```bash
asadmin create-system-properties dataverse.contact.captcha-provider=math
```

Use the established admin connection/password-file handling. After changing
provider/keys, reload the application through the existing procedure; local FRD
uses ./scripts/dev/dev-frd.sh. Payara/MicroProfile can retain earlier values until
the application reloads. There is no need to change data or authentication.
A clean browser refresh loads current resources.

## Verification

Focused tests:
```bash
mvn -Dtest=TurnstileVerifierTest,ContactCaptchaServiceTest,SendFeedbackDialogCaptchaTest,BrandingUtilTest -DskipIntegrationTests test
```

The initial run passed 35 tests with no failures/errors/skips. Mail delivery is
mocked. Tests cover hostname/action mismatch, malformed/error responses, missing/
oversized/expired/reused tokens, timeout/interruption, test-mode isolation,
incomplete configuration, rejected verification blocking mail, successful
verification reaching only the mocked native mail path, arithmetic bypass defense,
and the preceding support-name behavior.

Live verification of the deployed Java client in the application container against
Cloudflare's official passing/failing/spent dummy secrets returned VERIFIED,
REJECTED and REJECTED respectively. This probe never called the mail action.

Actual browser checks confirm automatic widget initialization through the native
support link, fresh verification after reopening, one vendor API script, retry,
native required-field rejection with message text preserved, and renewed
verification after form AJAX replacement. Native UA -> EN -> UA selects uk/en/uk
widget languages and translated status messages. At 375px and 320px there is no
horizontal page/content overflow, and the widget switches to compact rendering.

Successful live email delivery, authenticated sender/CC contexts and real-key
anti-bot decisions are not claimed tested. No support message was sent.

## Test-server and production rollout

No remote deployment has been performed. The user/administrator deploys remotely.

1. Create a Managed widget for uard2.dntb.gov.ua, with separate production keys
   for uard.dntb.gov.ua later. Restrict each to its intended hostname.
2. Confirm the active Payara application and reconcile the server's source/custom
   changes before building its normal deployment artifact. Do not use a stale
   target WAR; FRD updates only the local exploded application.
3. Deploy SendFeedbackDialog, JvmSettings, ContactCaptchaService,
   TurnstileVerifier, contactFormFragment.xhtml, the new script and matching
   stylesheet/messages together. Preserve the target's ua/uk filenames and
   remote-only translations. No database migration is required.
4. Configure real keys, the target hostname and test-mode=false. Turnstile
   permits an independent integration without Cloudflare DNS/proxy migration.
5. Check browser/CSP access to challenges.cloudflare.com and outbound HTTPS
   verification from Payara. Keep the existing trust/network policy.
6. Verify allowed test-mail delivery, failures/retry/expiration, reopen,
   locale switching, mobile layout and collection/dataset contacts before
   production rollout. Check privacy text for the added third-party verification.
7. Retain the previous artifact/configuration. Explicitly select math or restore
   the prior release if rollback is needed; never silently bypass verification.

Official references:
- [Widget rendering](https://developers.cloudflare.com/turnstile/get-started/client-side-rendering/)
- [Server verification](https://developers.cloudflare.com/turnstile/get-started/server-side-validation/)
- [Dummy test keys](https://developers.cloudflare.com/turnstile/troubleshooting/testing/)

Final checks also confirmed recovery after a blocked vendor script, clearing and
renewal through the expiry callback, and explicit arithmetic rollback without any
Cloudflare API script request. Turnstile was restored afterward. The expiry UI
callback was invoked in the isolated dummy-key QA browser rather than waiting
five minutes; real token expiry/reuse is covered by rejection tests and the
official spent-token dummy response.

The existing JvmSettingsTest suite also passed (3 tests, no failures/errors),
for 38 passing tests across the focused runs. Final local state is Turnstile
with official passing dummy keys after FRD and a fresh browser verification.

## Compact verification presentation

During normal loading/success, the widget provides visible status; our additional
status remains in an aria-live screen-reader-only region. The retry button is
hidden until a verification/script error occurs, when both the explanatory
message and retry control become visible. The panel uses 12px padding and 16px
vertical margins. Cloudflare's own widget, branding and dummy-key test banner
are unchanged. A free production widget does not show the dummy-key test banner.

Compact-layout verification: desktop success panel is 96px high with current
dummy keys; duplicate status is screen-reader-only and retry is hidden.
Blocking the vendor script reveals both error text and retry; successful retry
hides them again. The 320px viewport has no page/content horizontal overflow.
JavaScript syntax, XHTML parsing and Git whitespace checks pass. No mail sent.

On 2026-11-05, all 38 focused tests passed together before baseline integration.
The Git transition did not deploy the application or change server configuration.
