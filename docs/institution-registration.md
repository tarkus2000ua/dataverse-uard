# Institution registration

The homepage keeps all three carousels together. The university invitation follows
them, with its collection explanation always visible. The hero anchor, invitation
and footer lead to `/institution-registration.xhtml`.

The form is functional locally and on uard2 from 2026-11-25. It accepts applications for manual
review; collection creation, user accounts and permissions remain native Dataverse
operations performed by an administrator after review.

## Sources and presentation

- Form/review: `src/main/webapp/institution-{registration,applications}.xhtml`.
- Backend: `src/main/java/edu/harvard/iq/dataverse/uard/Institution*.java`.
- Browser behavior: `resources/js/uard/institution-registration.js` in the webapp.
- Labels: `src/main/java/propertyFiles/UardInstitutionRegistration*.properties`.
- Shared styles: `customization/branding/custom-stylesheet.css`.
- Homepage: `customization/homepage/template.html`, `locales/{en,uk}.json`,
  `content/home.{en,uk}.json`; footer: `customization/footer/links.json`.

Both pages use `dataverse_template.xhtml`, retaining native header, authentication,
support, locale switching and footer. Arial, navy/blue colors, 44px controls, 6px
corners and focus styling match the landing page. Form fields use two desktop
columns (three for personal names), and one on mobile.

Dataverse's native locale selects labels and server errors. The default bundle is
English; `_ua` and `_uk` are identical Ukrainian bundles. Keep them synchronized.
Bilingual institution fields retain their language attributes. There is no separate
frontend locale or persistent browser storage of personal data.

## Submission and storage

All 17 fields and authority/data-processing consent are required. Browser and server
validate the input. The server trims text, checks lengths/select values, a single
email address and an HTTP(S) URL. The supplied URL is never fetched. The client
retains entries on failure, disables submission while pending and shows a saved
reference only after the DB write succeeds. JavaScript is required; a noscript
message explains this.

Logos must be real, decodable JPG/JPEG, GIF or PNG images matching the filename
extension, at most 2 MiB and 16 million pixels. The bounded read and dimension check
precede image decoding; uploaded MIME headers are not trusted. Multipart requests
have a separate 3 MiB bounded-read limit including fields/overhead. This limit applies
only to the new servlet, preserving native dataset upload behavior. The existing
Jakarta Mail MIME parser reads this bounded payload, checks part count, field size,
duplicates and closing boundary. Container multipart configuration is avoided because
Payara can reject a large request before activating CDI and override its response.

`V6.12.0.1__uard_institution_applications.sql` adds one independent table and a pending
notification index through native startup Flyway. Existing tables/settings are
unchanged. Each row contains a UUID, timestamp, validated fields as JSON text,
private logo bytes/canonical MIME, consent version and notification state. These
are saved atomically in one JDBC INSERT using `java:app/jdbc/dataverse`.

A session-bound two-hour random token serves as CSRF token and idempotency key.
Retries, including concurrent requests, cannot overwrite the application or create
another row for that token (`ON CONFLICT DO NOTHING`). A per-view arithmetic
challenge provides basic spam protection, with eight failed attempts and at most
ten active forms per session. Reload after expiry. The form is not cacheable.
Existing contact-form CAPTCHA settings and behavior are unchanged.

## Review and notifications

`/institution-applications.xhtml` lists 50 applications per page. A superuser-only
link appears below the public form. `?id=<UUID>` opens all fields, the consent
record and notification state. Logos download through
`/institution-application-logo?id=<UUID>`. Both routes require native authentication
and a current superuser, with additional authorization checks in the service.
Guests and ordinary users cannot read applications or logos. Text is escaped;
logos are attachments with canonical filenames. Private responses use `no-store`,
and logo responses also use `nosniff`.

Native `MailServiceBean` sends to the configured support address, falling back to
the system address. The bilingual notification contains a UUID and protected review
link without personal data or attachments. No automatic applicant email is sent.
After commit, an async task attempts delivery. An EJB timer polls the durable queue
every minute; failures become eligible again after five minutes. Restart/redeploy
preserves queued work. A singleton serializes delivery on this single server.
Delivery is at least once: a crash after SMTP acceptance but before DB acknowledgement
may cause another notice with the same UUID. Mail failure never loses a submission.
A multi-server cluster would require a cross-node delivery claim.

Before a functional remote rollout, back up the target DB/WAR and verify native
SMTP, support address and public `dataverse.siteUrl`. Deploy classes, migration,
views, bundles, JavaScript and CSS together. The additive migration/table and all
application data must be retained on application rollback. Normal DB backups now
include applicant data and logos; restrict their access accordingly.

## Development and validation

Run `./scripts/dev/dev-frd.sh` after source changes. Mounted branding CSS refreshes
directly. Use homepage/footer builders only when their sources change. Do not run
the old demo conversion/finalization helpers: they remove backend artifacts.

```bash
mvn -Dtest=InstitutionApplicationValidatorTest,InstitutionRegistrationServletTest,InstitutionApplicationNotifierTest,InstitutionRegistrationMultipartTest -DskipUnitTests=false test
mvn -Dtest=InstitutionApplicationServiceIT -Dpostgresql.server.version=17 -DskipUnitTests=false test
node --test scripts/customization/tests/institution-registration.test.cjs
python3 scripts/customization/build-homepage.py --check
python3 scripts/customization/build-footer.py --check
```

This machine provides Node through the Windows Codex runtime (`node.exe` in
PowerShell); Git, Java builds, FRD and PostgreSQL tests run in WSL. Integration
tests use disposable Testcontainers PostgreSQL, never the Dataverse DB.

Verified 2026-11-25: 31 Java unit, three PostgreSQL integration and five client tests
passed. Live local HTTP checks covered server validation, spoofed/oversized logos,
CSRF, a single saved row with exact fields/logo, duplicate retry, one captured
support notification in MailDev and guest HTTP 403 for list/detail/download.
Browser checks covered native UA → EN → UA, empty-form focus, 19 required controls
(17 fields, consent, challenge), desktop and 320px layout without field overflow.
Authenticated browser review passed using the operator's existing superuser login:
the populated list opens, its demo link opens the correct UUID and all submitted
fields/consent, and the private logo downloads successfully. The downloaded PNG
is 9,383 bytes and its SHA-256 matches the submitted image. Anonymous list/logo
requests still return HTTP 403. Service authorization and data retrieval also
passed against PostgreSQL in integration tests.

The first authenticated nonempty-list check exposed an EL name collision: the
row variable `application` resolved to JSF's implicit ServletContext rather than
the submitted application. Renamed it to `institutionApplication` and redeployed
locally on 2026-11-25. XML validation, FRD and the authenticated list/detail/logo
browser checks passed, with no further EL rendering exceptions.
The explicitly requested demonstration university is retained with reference
`c8dc27d4-238b-436f-af07-8305c1d82d7e`; do not remove it with test cleanup.

Local server: http://localhost:8080/; mail catcher: http://localhost:1080/.
The reboot exposed stale Docker mounts. Existing WSL data was reconnected without
seeding or deleting volumes; 11 collections were retained. DB backups before
reconnection and migration are private local artifacts in
`work/institution-registration/`; never include them in a commit or release.

## uard2 deployment history

The demonstration deployed on 2026-11-24 was replaced by functional intake on
2026-11-25 at 12:56 Europe/Kyiv. The live form is
https://uard2.dntb.gov.ua/institution-registration.xhtml; protected review is at
https://uard2.dntb.gov.ua/institution-applications.xhtml.

The target's existing SMTP/support address is `uard@dntb.gov.ua`. Deployment checks
used invalid synthetic submissions and created no applications or external emails.
At a subsequent explicit user request, a separate fictional demo was submitted on
uard2 on 2026-11-25 at 13:04 Europe/Kyiv:
`87172605-5d37-403f-ae22-fa9ed5d7fd1a`. All 16 text/select fields, logo and consent
were verified in the remote DB. Its support notification was accepted by SMTP on
the first attempt. Inbox receipt and authenticated remote review were not checked.
The original local reference `c8dc27d4-238b-436f-af07-8305c1d82d7e` remains local;
the new remote application was submitted through the deployed form, not DB copying.
See [functional release and rollback](remote-deployment.md#functional-institution-registration-on-uard2-2026-11-25).
