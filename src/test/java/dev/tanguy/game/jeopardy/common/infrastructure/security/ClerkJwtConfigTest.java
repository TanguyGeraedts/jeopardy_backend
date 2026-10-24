package dev.tanguy.game.jeopardy.common.infrastructure.security;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Clerk-style tokens against a fake Clerk instance (a local server publishing the JWKS). */
class ClerkJwtConfigTest {

    private static HttpServer clerk;
    private static String issuer;
    private static RSAKey key;
    private static RSAKey otherKey;

    @BeforeAll
    static void startFakeClerk() throws Exception {
        key = new RSAKeyGenerator(2048).keyID("ins_test").generate();
        otherKey = new RSAKeyGenerator(2048).keyID("ins_test").generate();
        byte[] jwks = new JWKSet(key.toPublicJWK()).toString().getBytes(StandardCharsets.UTF_8);

        clerk = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        clerk.createContext("/.well-known/jwks.json", exchange -> {
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, jwks.length);
            exchange.getResponseBody().write(jwks);
            exchange.close();
        });
        clerk.start();
        issuer = "http://127.0.0.1:" + clerk.getAddress().getPort();
    }

    @AfterAll
    static void stopFakeClerk() {
        clerk.stop(0);
    }

    private static JwtDecoder decoder() {
        return new ClerkJwtConfig().jwtDecoder(new ClerkProperties(issuer, List.of("http://localhost:3000")));
    }

    private static String token(RSAKey signingKey, String tokenIssuer, String azp, Instant expires) throws Exception {
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .issuer(tokenIssuer)
                .subject("user_2abcDEF")
                .claim("azp", azp)
                .claim("sid", "sess_123")
                .issueTime(Date.from(Instant.now().minusSeconds(5)))
                .notBeforeTime(Date.from(Instant.now().minusSeconds(5)))
                .expirationTime(Date.from(expires))
                .build();
        SignedJWT jwt = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(signingKey.getKeyID()).build(), claims);
        jwt.sign(new RSASSASigner(signingKey));
        return jwt.serialize();
    }

    private static Instant inAMinute() {
        return Instant.now().plusSeconds(60);
    }

    @Test
    void acceptsAClerkTokenFromOurFrontend() throws Exception {
        Jwt jwt = decoder().decode(token(key, issuer, "http://localhost:3000", inAMinute()));

        assertThat(jwt.getSubject()).isEqualTo("user_2abcDEF");
    }

    @Test
    void rejectsATokenIssuedToAnotherSite() throws Exception {
        assertThatThrownBy(() -> decoder().decode(token(key, issuer, "https://evil.example", inAMinute())))
                .isInstanceOf(JwtException.class)
                .hasMessageContaining("not an allowed frontend");
    }

    @Test
    void rejectsAnotherIssuerAnExpiredTokenAndAForgedSignature() throws Exception {
        JwtDecoder decoder = decoder();

        assertThatThrownBy(() -> decoder.decode(token(key, "https://other.clerk.accounts.dev", "http://localhost:3000", inAMinute())))
                .isInstanceOf(JwtException.class);
        assertThatThrownBy(() -> decoder.decode(token(key, issuer, "http://localhost:3000", Instant.now().minusSeconds(300))))
                .isInstanceOf(JwtException.class);
        assertThatThrownBy(() -> decoder.decode(token(otherKey, issuer, "http://localhost:3000", inAMinute())))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void refusesToStartWithoutARealIssuer() {
        assertThatThrownBy(() -> new ClerkJwtConfig().jwtDecoder(new ClerkProperties("https://YOUR-INSTANCE.clerk.accounts.dev", List.of())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("app.security.clerk.issuer");
    }

    @Test
    void aClerkUserIdBecomesAStableOwnerId() {
        AuthenticatedUser user = new AuthenticatedUser("user_2abcDEF", null, List.of());

        assertThat(user.ownerId()).isEqualTo(new AuthenticatedUser("user_2abcDEF", null, List.of()).ownerId());
    }
}
