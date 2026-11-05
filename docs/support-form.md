# UaRD support/contact dialog

Updated 2026-11-02. Target: Dataverse 6.12.

## Title formatting

The external Ukrainian bundle previously contained
`contact.header=Зв'язатися з {0}`. BrandingUtil.getContactHeader supplies the
configured support-team name as parameter 0 through BundleUtil/MessageFormat.
The single ASCII apostrophe starts a quoted section, suppressing substitution
and removing the displayed apostrophe.

The entry now uses the Ukrainian typographic apostrophe:
`contact.header=Зв’язатися з {0}`.
Two ASCII apostrophes would also work with MessageFormat.
The earlier OAuth fix concerned auth.providers.blurb, a separate key.
Do not change the unformatted email placeholder merely because it has an apostrophe.

Original apostrophe fix: merge contact.header into the server's existing Ukrainian bundle,
retaining its ua/uk filename and other translations. Reload through the established
Payara procedure to clear bundle caches. No support-team setting change is needed.
No remote deployment was performed.

## Styling

Scoped #contactDialog rules in customization/branding/custom-stylesheet.css reuse
UaRD navy, blue, pale-blue surfaces, rounded borders and visible keyboard focus.
Fields/buttons/close control have a minimum 44px height. Mobile labels stack;
dialog width is constrained and content scrolls on shorter viewports.
The arithmetic validation is grouped visually, with the original question retained.

The native contactFormFragment.xhtml is unchanged: support/collection/
dataset recipients, optional CC, authenticated sender, JSF validation, arithmetic
answer, send action and cancel behavior remain native. Styling applies to all
contexts of this shared dialog, not other dialogs.

Mounted CSS updates on refresh; external bundles require application reload.
Local reload uses ./scripts/dev/dev-frd.sh. For remote deployment publish the
reviewed stylesheet (preserving remote-only additions) and merge the bundle
entries described below together with the branding helper update. Keep changes
in the maintained deployment artifacts.

## CAPTCHA replacement options

The original native form uses a view-scoped addition question and a server-side
validateUserSum method. The local Turnstile integration now adds a configurable
provider and server verification before sending mail, preserving JSF validation
and AJAX retry behavior. See turnstile-local.md.

- Cloudflare Turnstile: recommended hosted option for minimal user interaction.
  Requires site/secret keys and server-side Siteverify checks.
  https://developers.cloudflare.com/turnstile/get-started/
- ALTCHA open-source core: self-hosted proof of work, with no external verification
  service. Requires server challenge generation, verification and replay protection.
  More code/operations to maintain in this Java application.
  https://altcha.org/docs/integration/quick-start/
- hCaptcha: hosted alternative, with checkbox/invisible modes; challenges can
  interrupt users depending on the mode/plan. Requires keys and server verification.
  https://docs.hcaptcha.com/
  https://docs.hcaptcha.com/invisible

Cloudflare Turnstile is now integrated locally with official dummy keys.
See turnstile-local.md for configuration, verification and remote rollout.
Arithmetic remains the default on installations without explicit configuration.
Integrate widget initialization/reset with PrimeFaces dialog AJAX updates, native UA/EN labels, expired/reused tokens, server
timeouts and failed form validation. Keep secrets outside public resources/Git.
Rate limiting complements any CAPTCHA. Only local contact CAPTCHA configuration was changed; authentication settings
are untouched. Arithmetic remains available through explicit provider selection.

## Verification scope

Verify the formatted Ukrainian title, native UA/EN switching, support opening/
closing, field focus and desktop/mobile layout without sending a support message.
No authenticated account or outbound mail is needed for this presentation check.

Verified locally on 2026-11-02:

- Java 21 MessageFormat reproduced the old literal {0} and substituted the
  configured support name with the corrected bundle entry.
- Required fast redeploy succeeded on the existing running stack.
- Native UA -> EN -> UA switching produced the correct title and field labels.
- At 1440x1000, 375x812 and 320x640, the dialog stayed within the viewport
  without horizontal overflow. Short viewports scroll to both action buttons.
- Desktop and mobile screenshots were inspected; email receives initial focus,
  Tab moves to Subject, and Cancel closes the dialog.
- Git whitespace checks pass. No message was sent, CAPTCHA solved, provider
  configured, or account/data/settings changed. Authenticated sender/CC and
  successful mail delivery were not exercised by these presentation checks.

## Ukrainian support-name order and case

The generated Ukrainian recipient label is now **Підтримка Root**, and the
dialog title is **Зв’язатися з підтримкою Root**. Root is dynamic, coming from
the native root collection name; it is not hardcoded.

Optional Ukrainian entries:

- org.support.team.root=Підтримка {0}
- org.contact.header.root=Зв’язатися з підтримкою {0}

BrandingUtil uses these entries only for generated names. A personal name on the
configured support email retains precedence. Locales without the optional keys
keep their existing wording/order, including English. No root-name, mail-address,
authentication, or database setting is changed. The generated Ukrainian name also
appears wherever the native support name is reused, including support-link labels
and email branding. Collection/dataset-specific contact titles remain unchanged.

This follow-up requires the BrandingUtil Java change plus the two Ukrainian keys,
not a bundle-only replacement. Use the normal rebuilt remote application artifact
when requested; local source changes use fast redeploy. See upstream-modifications.md.

Follow-up verification: BrandingUtilTest passed (22 tests, no failures/errors),
and fast redeploy succeeded. Live UA -> EN -> UA checks confirmed the root API
currently reports UaRD: Ukrainian title Зв’язатися з підтримкою UaRD and recipient
Підтримка UaRD; English Contact UaRD Support and UaRD Support. Root above is an
example, not a configured literal. No support message or settings change was made.
