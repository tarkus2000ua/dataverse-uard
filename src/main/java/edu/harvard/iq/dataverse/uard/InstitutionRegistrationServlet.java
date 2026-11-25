package edu.harvard.iq.dataverse.uard;

import edu.harvard.iq.dataverse.util.BundleUtil;
import edu.harvard.iq.dataverse.util.json.JsonUtil;
import jakarta.ejb.EJB;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;
import java.util.logging.Logger;

@WebServlet("/institution-registration-submit")
public class InstitutionRegistrationServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(InstitutionRegistrationServlet.class.getName());
    @EJB InstitutionApplicationService applications;
    @EJB InstitutionApplicationNotifier notifier;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("X-Content-Type-Options", "nosniff");
        var httpSession = request.getSession(false);
        var bundle = BundleUtil.getResourceBundle("UardInstitutionRegistration", Locale.ENGLISH);
        if (httpSession == null) { error(response, bundle, 403, "errorExpired", ""); return; }
        // Serialize submissions from a session; the DB key also prevents duplicates after a lost response.
        synchronized (httpSession) {
            String token = request.getHeader("X-Uard-Token");
            var challenge = InstitutionRegistrationPage.challenges(httpSession).get(token);
            if (challenge != null) bundle = BundleUtil.getResourceBundle("UardInstitutionRegistration", Locale.forLanguageTag(challenge.locale));
            if (challenge == null || challenge.expires.isBefore(Instant.now()) || challenge.failures >= 8) {
                error(response, bundle, 403, "errorExpired", ""); return;
            }
            if (challenge.completed) { success(response, token); return; }
            if (request.getContentLengthLong() > InstitutionRegistrationMultipart.MAX_REQUEST) {
                error(response, bundle, 413, "logoTooLarge", "logo"); return;
            }
            try {
                var parsed = InstitutionRegistrationMultipart.parse(request.getInputStream(), request.getContentType());
                if (!challenge.accepts(parsed.fields().get("captcha"))) {
                    challenge.failures++;
                    error(response, bundle, 422, "errorCaptcha", "captcha"); return;
                }
                try (var stream = new ByteArrayInputStream(parsed.logo())) {
                    var validated = InstitutionApplicationValidator.validate(parsed.fields(), "on".equals(parsed.fields().get("consent")),
                            stream, parsed.logo().length, parsed.filename());
                    applications.save(UUID.fromString(token), validated);
                }
                challenge.completed = true;
                // A failure to start the immediate task cannot undo the saved application; the timer retries.
                try { notifier.wake(); } catch (RuntimeException ex) { LOGGER.warning("Institution notification deferred to timer"); }
                success(response, token);
            } catch (InstitutionApplicationValidator.Invalid ex) {
                error(response, bundle, "logoTooLarge".equals(ex.messageKey()) ? 413 : 422, ex.messageKey(), ex.field());
            } catch (IllegalStateException ex) {
                error(response, bundle, 413, "logoTooLarge", "logo");
            } catch (SQLException ex) {
                LOGGER.warning("Institution application storage failed (SQLState " + ex.getSQLState() + ")");
                error(response, bundle, 503, "errorStorage", "");
            } catch (jakarta.mail.MessagingException | IOException ex) {
                error(response, bundle, 400, "errorUpload", "logo");
            }
        }
    }

    private void success(HttpServletResponse response, String reference) throws IOException {
        response.getWriter().write(JsonUtil.createObjectBuilder().add("ok", true).add("reference", reference).build().toString());
    }

    private void error(HttpServletResponse response, java.util.ResourceBundle bundle, int status, String key, String field) throws IOException {
        response.setStatus(status);
        response.getWriter().write(JsonUtil.createObjectBuilder().add("ok", false).add("message", bundle.getString(key))
                .add("field", field).build().toString());
    }
}
