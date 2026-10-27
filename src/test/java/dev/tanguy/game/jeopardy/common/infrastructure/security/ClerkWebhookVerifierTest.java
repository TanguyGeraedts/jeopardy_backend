package dev.tanguy.game.jeopardy.common.infrastructure.security;

import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClerkWebhookVerifierTest {

    static final byte[] KEY = "test-webhook-key-that-is-32-byte".getBytes(StandardCharsets.UTF_8);
    static final String SECRET = "whsec_" + Base64.getEncoder().encodeToString(KEY);
    static final Instant NOW = Instant.parse("2026-10-27T12:00:00Z");
    static final String ID = "msg_2abc";
    static final String BODY = "{\"type\":\"user.deleted\",\"data\":{\"id\":\"user_123\",\"deleted\":true}}";

    final ClerkWebhookVerifier verifier = new ClerkWebhookVerifier(SECRET, Duration.ofMinutes(5), Clock.fixed(NOW, ZoneOffset.UTC));

    /** Signs like Svix does: HMAC-SHA256 over "id.timestamp.body", sent as "v1,base64". */
    static String sign(byte[] key, String id, String timestamp, String body) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(key, "HmacSHA256"));
        byte[] signature = mac.doFinal((id + "." + timestamp + "." + body).getBytes(StandardCharsets.UTF_8));
        return "v1," + Base64.getEncoder().encodeToString(signature);
    }

    @Test
    void acceptsAFreshCorrectlySignedWebhook() throws Exception {
        String timestamp = String.valueOf(NOW.getEpochSecond());
        assertThat(verifier.isValid(ID, timestamp, sign(KEY, ID, timestamp, BODY), BODY)).isTrue();
    }

    @Test
    void acceptsWhenAnyOfSeveralSignaturesMatches() throws Exception {
        String timestamp = String.valueOf(NOW.getEpochSecond());
        String header = "v1,bm90LXRoZS1yaWdodC1vbmU= " + sign(KEY, ID, timestamp, BODY);
        assertThat(verifier.isValid(ID, timestamp, header, BODY)).isTrue();
    }

    @Test
    void rejectsATamperedBody() throws Exception {
        String timestamp = String.valueOf(NOW.getEpochSecond());
        String signature = sign(KEY, ID, timestamp, BODY);
        assertThat(verifier.isValid(ID, timestamp, signature, BODY.replace("user_123", "user_999"))).isFalse();
    }

    @Test
    void rejectsAnotherSecret() throws Exception {
        String timestamp = String.valueOf(NOW.getEpochSecond());
        byte[] otherKey = "another-key-that-is-also-32-byte".getBytes(StandardCharsets.UTF_8);
        assertThat(verifier.isValid(ID, timestamp, sign(otherKey, ID, timestamp, BODY), BODY)).isFalse();
    }

    @Test
    void rejectsAnOldTimestampAsAReplay() throws Exception {
        String timestamp = String.valueOf(NOW.minus(Duration.ofMinutes(6)).getEpochSecond());
        assertThat(verifier.isValid(ID, timestamp, sign(KEY, ID, timestamp, BODY), BODY)).isFalse();
    }

    @Test
    void rejectsMissingHeadersAndGarbage() {
        String timestamp = String.valueOf(NOW.getEpochSecond());
        assertThat(verifier.isValid(null, timestamp, "v1,abc", BODY)).isFalse();
        assertThat(verifier.isValid(ID, "not-a-number", "v1,abc", BODY)).isFalse();
        assertThat(verifier.isValid(ID, timestamp, "v1,%%%not-base64", BODY)).isFalse();
    }

    @Test
    void rejectsEverythingWhenNoSecretIsConfigured() throws Exception {
        ClerkWebhookVerifier unconfigured = new ClerkWebhookVerifier("", Duration.ofMinutes(5), Clock.fixed(NOW, ZoneOffset.UTC));
        String timestamp = String.valueOf(NOW.getEpochSecond());

        assertThat(unconfigured.isConfigured()).isFalse();
        assertThat(unconfigured.isValid(ID, timestamp, sign(KEY, ID, timestamp, BODY), BODY)).isFalse();
    }

    @Test
    void failsFastOnAMalformedSecret() {
        assertThatThrownBy(() -> new ClerkWebhookVerifier("whsec_%%%", Duration.ofMinutes(5), Clock.systemUTC()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("signing-secret");
    }
}
