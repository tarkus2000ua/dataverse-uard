package edu.harvard.iq.dataverse.feedback;

import java.io.IOException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TurnstileVerifierTest {
    private TurnstileVerifier verifier(String body) {
        return new TurnstileVerifier((secret, token) -> new TurnstileVerifier.Response(200, body));
    }

    @Test
    void acceptsOnlyMatchingRealHostnameAndAction() {
        TurnstileVerifier valid = verifier("{\"success\":true,\"hostname\":\"uard2.dntb.gov.ua\",\"action\":\"contact_form\"}");
        assertEquals(TurnstileVerifier.Result.VERIFIED, valid.verify("secret", "token", "uard2.dntb.gov.ua", false));
        assertEquals(TurnstileVerifier.Result.REJECTED, valid.verify("secret", "token", "uard.dntb.gov.ua", false));
        assertEquals(TurnstileVerifier.Result.REJECTED,
                verifier("{\"success\":true,\"hostname\":\"uard2.dntb.gov.ua\",\"action\":\"login\"}")
                        .verify("secret", "token", "uard2.dntb.gov.ua", false));
        assertEquals(TurnstileVerifier.Result.REJECTED,
                verifier("{\"success\":true}").verify("secret", "token", "uard2.dntb.gov.ua", false));
    }

    @Test
    void rejectsMissingOversizedExpiredAndReusedTokens() {
        TurnstileVerifier noCall = new TurnstileVerifier((secret, token) -> {
            fail("Missing/oversized token must not call Cloudflare");
            return null;
        });
        for (String token : new String[] {null, "", " ", "x".repeat(2049)}) {
            assertEquals(TurnstileVerifier.Result.REJECTED, noCall.verify("secret", token, "localhost", false));
        }
        assertEquals(TurnstileVerifier.Result.REJECTED,
                verifier("{\"success\":false,\"error-codes\":[\"timeout-or-duplicate\"]}")
                        .verify("secret", "spent-token", "localhost", false));
    }

    @Test
    void dummyResponseRequiresExplicitTestModeAndMetadata() {
        TurnstileVerifier dummy = verifier("{\"success\":true,\"hostname\":\"example.com\",\"metadata\":{\"result_with_testing_key\":true}}");
        assertEquals(TurnstileVerifier.Result.VERIFIED, dummy.verify("dummy", "token", "localhost", true));
        assertEquals(TurnstileVerifier.Result.REJECTED, dummy.verify("dummy", "token", "example.com", false));
        assertEquals(TurnstileVerifier.Result.REJECTED,
                verifier("{\"success\":true}").verify("dummy", "token", "localhost", true));
    }

    @Test
    void failsClosedOnMalformedResponsesHttpErrorsAndNetworkFailure() {
        for (String body : new String[] {"{}", "null", "not json", "{\"success\":\"true\"}", " ".repeat(16385)}) {
            assertEquals(TurnstileVerifier.Result.UNAVAILABLE,
                    verifier(body).verify("secret", "token", "localhost", false));
        }
        assertEquals(TurnstileVerifier.Result.UNAVAILABLE,
                new TurnstileVerifier((s, t) -> new TurnstileVerifier.Response(503, "{}"))
                        .verify("secret", "token", "localhost", false));
        assertEquals(TurnstileVerifier.Result.UNAVAILABLE,
                new TurnstileVerifier((s, t) -> { throw new IOException("timeout"); })
                        .verify("secret", "token", "localhost", false));
    }

    @Test
    void preservesInterruptWhenVerificationIsInterrupted() {
        try {
            assertEquals(TurnstileVerifier.Result.UNAVAILABLE,
                    new TurnstileVerifier((s, t) -> { throw new InterruptedException(); })
                            .verify("secret", "token", "localhost", false));
            assertTrue(Thread.currentThread().isInterrupted());
        } finally {
            Thread.interrupted();
        }
    }
}
