package dev.tanguy.game.jeopardy.common.infrastructure.security;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

/**
 * Checks a Clerk webhook's Svix signature: an HMAC-SHA256 over "{svix-id}.{svix-timestamp}.{body}" with the endpoint's
 * signing secret, sent as "v1,{base64}" (several, space separated, while a secret is being rotated).
 * See https://docs.svix.com/receiving/verifying-payloads/how-manual
 */
public class ClerkWebhookVerifier {

    private static final String SECRET_PREFIX = "whsec_";
    private static final String VERSION_PREFIX = "v1,";

    private final byte[] key;
    private final Duration tolerance;
    private final Clock clock;

    /** secret null or blank = not configured: every webhook is rejected. */
    public ClerkWebhookVerifier(String secret, Duration tolerance, Clock clock) {
        this.key = secret == null || secret.isBlank() ? null : decodeSecret(secret.strip());
        this.tolerance = tolerance;
        this.clock = clock;
    }

    public boolean isConfigured() {
        return key != null;
    }

    public boolean isValid(String id, String timestamp, String signatures, String body) {
        if (key == null || id == null || timestamp == null || signatures == null || body == null) {
            return false;
        }
        if (!isFresh(timestamp)) {
            return false;
        }
        byte[] expected = sign(id + "." + timestamp + "." + body);
        for (String candidate : signatures.split(" ")) {
            if (!candidate.startsWith(VERSION_PREFIX)) {
                continue;
            }
            try {
                byte[] given = Base64.getDecoder().decode(candidate.substring(VERSION_PREFIX.length()));
                if (MessageDigest.isEqual(expected, given)) {
                    return true;
                }
            } catch (IllegalArgumentException ignored) {
                // not base64: try the next one
            }
        }
        return false;
    }

    private boolean isFresh(String timestamp) {
        try {
            Instant sent = Instant.ofEpochSecond(Long.parseLong(timestamp));
            return Duration.between(sent, clock.instant()).abs().compareTo(tolerance) <= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private byte[] sign(String content) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            return mac.doFinal(content.getBytes(StandardCharsets.UTF_8));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("HmacSHA256 is not available", e);
        }
    }

    private static byte[] decodeSecret(String secret) {
        String encoded = secret.startsWith(SECRET_PREFIX) ? secret.substring(SECRET_PREFIX.length()) : secret;
        try {
            return Base64.getDecoder().decode(encoded);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("app.security.clerk-webhook.signing-secret is not a valid whsec_... secret", e);
        }
    }
}
