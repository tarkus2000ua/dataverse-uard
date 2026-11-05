package edu.harvard.iq.dataverse.feedback;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/** Server-only verification; never logs credentials, response tokens or form text. */
public class TurnstileVerifier {
    public enum Result { VERIFIED, REJECTED, UNAVAILABLE }

    record Response(int status, String body) { }
    @FunctionalInterface
    interface Transport {
        Response post(String secret, String token) throws IOException, InterruptedException;
    }

    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .followRedirects(HttpClient.Redirect.NEVER).build();
    private static final ObjectMapper JSON = new ObjectMapper();
    private final Transport transport;

    public TurnstileVerifier() {
        this(TurnstileVerifier::request);
    }

    TurnstileVerifier(Transport transport) {
        this.transport = transport;
    }

    public Result verify(String secret, String token, String hostname, boolean testMode) {
        if (token == null || token.isBlank() || token.length() > 2048) {
            return Result.REJECTED;
        }
        try {
            Response response = transport.post(secret, token);
            if (response.status() != 200 || response.body() == null || response.body().length() > 16384) {
                return Result.UNAVAILABLE;
            }
            JsonNode data = JSON.readTree(response.body());
            if (data == null || !data.path("success").isBoolean()) {
                return Result.UNAVAILABLE;
            }
            if (!data.path("success").booleanValue()) {
                return Result.REJECTED;
            }
            boolean testingResponse = data.path("metadata").path("result_with_testing_key").asBoolean(false);
            // Official dummy responses use a fixed hostname and may omit action.
            // Only explicit, loopback-restricted test configuration may accept them.
            if (testMode) {
                return testingResponse ? Result.VERIFIED : Result.REJECTED;
            }
            return !testingResponse && data.path("hostname").isTextual()
                    && hostname.equalsIgnoreCase(data.path("hostname").textValue())
                    && ContactCaptchaService.ACTION.equals(data.path("action").asText())
                    ? Result.VERIFIED : Result.REJECTED;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return Result.UNAVAILABLE;
        } catch (IOException | RuntimeException ex) {
            return Result.UNAVAILABLE;
        }
    }

    private static Response request(String secret, String token) throws IOException, InterruptedException {
        String body = "secret=" + URLEncoder.encode(secret, StandardCharsets.UTF_8)
                + "&response=" + URLEncoder.encode(token, StandardCharsets.UTF_8);
        HttpRequest request = HttpRequest.newBuilder(
                URI.create("https://challenges.cloudflare.com/turnstile/v0/siteverify"))
                .timeout(Duration.ofSeconds(8))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body)).build();
        HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        return new Response(response.statusCode(), response.body());
    }
}
