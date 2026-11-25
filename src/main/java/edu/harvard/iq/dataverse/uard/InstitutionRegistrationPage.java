package edu.harvard.iq.dataverse.uard;

import edu.harvard.iq.dataverse.DataverseSession;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpSession;
import java.io.Serializable;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/** A per-view, session-bound CSRF/idempotency token; no form contents live in the session. */
@Named
@RequestScoped
public class InstitutionRegistrationPage {
    private static final String SESSION_KEY = InstitutionRegistrationPage.class.getName();
    private static final SecureRandom RANDOM = new SecureRandom();
    @Inject DataverseSession dataverseSession;
    private Challenge challenge;

    public static final class Challenge implements Serializable {
        final String locale;
        final String token = UUID.randomUUID().toString();
        final Instant expires = Instant.now().plusSeconds(7200);
        final int left = RANDOM.nextInt(8) + 2;
        final int right = RANDOM.nextInt(8) + 2;
        int failures;
        boolean completed;
        Challenge() { this("en"); }
        Challenge(String locale) { this.locale = locale; }
        boolean accepts(String answer) { return Integer.toString(left + right).equals(answer == null ? "" : answer.strip()); }
    }

    @SuppressWarnings("unchecked")
    static Map<String, Challenge> challenges(HttpSession session) {
        var challenges = (Map<String, Challenge>) session.getAttribute(SESSION_KEY);
        if (challenges == null) {
            challenges = new LinkedHashMap<>();
            session.setAttribute(SESSION_KEY, challenges);
        }
        return challenges;
    }

    private Challenge challenge() {
        if (challenge == null) {
            var context = FacesContext.getCurrentInstance().getExternalContext();
            context.setResponseHeader("Cache-Control", "no-store");
            HttpSession session = (HttpSession) context.getSession(true);
            synchronized (session) {
                Map<String, Challenge> challenges = challenges(session);
                challenges.values().removeIf(item -> item.expires.isBefore(Instant.now()));
                while (challenges.size() >= 10) challenges.remove(challenges.keySet().iterator().next());
                challenge = new Challenge(dataverseSession.getLocaleCode());
                challenges.put(challenge.token, challenge);
            }
        }
        return challenge;
    }

    public String getToken() { return challenge().token; }
    public String getQuestion() { return challenge().left + " + " + challenge().right + " ="; }
}
