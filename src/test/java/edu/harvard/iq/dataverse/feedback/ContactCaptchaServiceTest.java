package edu.harvard.iq.dataverse.feedback;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ContactCaptchaServiceTest {
    private static final String SITE = "1x00000000000000000000AA";
    private static final String SECRET = "1x0000000000000000000000000000000AA";

    @Test
    void localTestModeRequiresDummyKeysAndLoopbackConfigurationAndRequest() {
        var local = new ContactCaptchaService.Settings("turnstile", SITE, SECRET, "localhost", true);
        assertTrue(ContactCaptchaService.configured(local, "localhost"));
        assertFalse(ContactCaptchaService.configured(local, "uard2.dntb.gov.ua"));
        assertFalse(ContactCaptchaService.configured(
                new ContactCaptchaService.Settings("turnstile", SITE, SECRET, "uard2.dntb.gov.ua", true), "localhost"));
        assertFalse(ContactCaptchaService.configured(
                new ContactCaptchaService.Settings("turnstile", "real-site", "real-secret", "localhost", true), "localhost"));
    }

    @Test
    void liveModeRejectsTestKeysAndIncompleteOrUnknownConfiguration() {
        assertTrue(ContactCaptchaService.configured(
                new ContactCaptchaService.Settings("turnstile", "real-site", "real-secret", "uard2.dntb.gov.ua", false), "uard2.dntb.gov.ua"));
        for (var config : new ContactCaptchaService.Settings[] {
                new ContactCaptchaService.Settings("turnstile", SITE, SECRET, "localhost", false),
                new ContactCaptchaService.Settings("turnstile", "real-site", SECRET, "localhost", false),
                new ContactCaptchaService.Settings("turnstile", SITE, "real-secret", "localhost", false),
                new ContactCaptchaService.Settings("turnstile", "real-site", "", "localhost", false),
                new ContactCaptchaService.Settings("turnstile", "real-site", "real-secret", "", false),
                new ContactCaptchaService.Settings("typo", "real-site", "real-secret", "localhost", false)}) {
            assertFalse(ContactCaptchaService.configured(config, "localhost"));
        }
    }

    @Test
    void invalidConfigurationCannotCallVerifierOrFallBackToMath() {
        TurnstileVerifier verifier = mock(TurnstileVerifier.class);
        ContactCaptchaService service = new ContactCaptchaService(verifier) {
            @Override protected Settings settings() {
                return new Settings("turnstile", SITE, "", "localhost", true);
            }
        };
        assertFalse(service.isMathEnabled());
        assertEquals(TurnstileVerifier.Result.UNAVAILABLE, service.verify("token", "localhost"));
        verifyNoInteractions(verifier);
    }

    @Test
    void explicitlySelectedMathAndConfiguredTurnstileUseDifferentPaths() {
        TurnstileVerifier verifier = mock(TurnstileVerifier.class);
        ContactCaptchaService service = new ContactCaptchaService(verifier) {
            @Override protected Settings settings() {
                return new Settings("turnstile", SITE, SECRET, "localhost", true);
            }
        };
        when(verifier.verify(SECRET, "token", "localhost", true)).thenReturn(TurnstileVerifier.Result.VERIFIED);
        assertEquals(TurnstileVerifier.Result.VERIFIED, service.verify("token", "localhost"));
        ContactCaptchaService math = new ContactCaptchaService(verifier) {
            @Override protected Settings settings() {
                return new Settings("math", "", "", "", false);
            }
        };
        assertTrue(math.isMathEnabled());
    }
}
