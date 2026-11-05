package edu.harvard.iq.dataverse.feedback;

import edu.harvard.iq.dataverse.settings.JvmSettings;
import jakarta.ejb.Stateless;
import java.util.Locale;
import java.util.Set;

@Stateless
public class ContactCaptchaService {
    public static final String ACTION = "contact_form";
    private static final Set<String> LOOPBACK = Set.of("localhost", "127.0.0.1", "::1", "[::1]");
    private static final Set<String> TEST_SITE_KEYS = Set.of(
            "1x00000000000000000000AA", "2x00000000000000000000AB",
            "1x00000000000000000000BB", "2x00000000000000000000BB", "3x00000000000000000000FF");
    private static final Set<String> TEST_SECRET_KEYS = Set.of(
            "1x0000000000000000000000000000000AA",
            "2x0000000000000000000000000000000AA",
            "3x0000000000000000000000000000000AA");
    private final TurnstileVerifier verifier;

    record Settings(String provider, String siteKey, String secretKey, String hostname, boolean testMode) { }

    public ContactCaptchaService() {
        this(new TurnstileVerifier());
    }

    ContactCaptchaService(TurnstileVerifier verifier) {
        this.verifier = verifier;
    }

    protected Settings settings() {
        return new Settings(
                JvmSettings.CONTACT_CAPTCHA_PROVIDER.lookupOptional().orElse("math").trim().toLowerCase(Locale.ROOT),
                JvmSettings.CONTACT_TURNSTILE_SITE_KEY.lookupOptional().orElse("").trim(),
                JvmSettings.CONTACT_TURNSTILE_SECRET_KEY.lookupOptional().orElse("").trim(),
                JvmSettings.CONTACT_TURNSTILE_HOSTNAME.lookupOptional().orElse("").trim(),
                JvmSettings.CONTACT_TURNSTILE_TEST_MODE.lookupOptional().map(Boolean::parseBoolean).orElse(false));
    }

    public boolean isMathEnabled() {
        return "math".equals(settings().provider());
    }

    public String getSiteKey() {
        return settings().siteKey();
    }

    public TurnstileVerifier.Result verify(String token, String requestHostname) {
        Settings config = settings();
        if (!configured(config, requestHostname)) {
            return TurnstileVerifier.Result.UNAVAILABLE;
        }
        return verifier.verify(config.secretKey(), token, config.hostname(), config.testMode());
    }

    static boolean configured(Settings config, String requestHostname) {
        if (!"turnstile".equals(config.provider()) || config.siteKey().isBlank()
                || config.secretKey().isBlank() || config.hostname().isBlank()) {
            return false;
        }
        if (config.testMode()) {
            return LOOPBACK.contains(config.hostname().toLowerCase(Locale.ROOT))
                    && requestHostname != null && LOOPBACK.contains(requestHostname.toLowerCase(Locale.ROOT))
                    && TEST_SITE_KEYS.contains(config.siteKey()) && TEST_SECRET_KEYS.contains(config.secretKey());
        }
        return !TEST_SITE_KEYS.contains(config.siteKey()) && !TEST_SECRET_KEYS.contains(config.secretKey());
    }
}
