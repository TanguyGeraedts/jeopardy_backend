package dev.tanguy.game.jeopardy.gameplay.adapter.out.lobby;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import dev.tanguy.game.jeopardy.gameplay.domain.exception.session.InvalidHandoffException;
import dev.tanguy.game.jeopardy.gameplay.port.out.lobby.LobbyHandoff;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Verifies the handoff JWT the lobby signs when the host starts the game:
 * RS256 only, issuer, audience ("game-server") and expiry are all checked.
 * Not a JwtDecoder bean on purpose: that would replace the decoder used for normal API logins.
 */
@Component
public class LobbyHandoffVerifier {

    private final JwtDecoder decoder;
    private final ObjectMapper objectMapper;

    public LobbyHandoffVerifier(LobbyProperties properties, ObjectMapper objectMapper) throws Exception {
        Objects.requireNonNull(properties.publicKeyLocation(),
                "app.lobby.public-key-location must be set (the lobby's public.pem)");

        NimbusJwtDecoder nimbus = NimbusJwtDecoder.withPublicKey(readPublicKey(properties)).build(); // RS256
        OAuth2TokenValidator<Jwt> audience = new JwtClaimValidator<List<String>>(
                JwtClaimNames.AUD, aud -> aud != null && aud.contains(properties.handoffAudience()));
        nimbus.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefaultWithIssuer(properties.issuer()), audience));

        this.decoder = nimbus;
        this.objectMapper = objectMapper;
    }

    public LobbyHandoff verify(String token) {
        Jwt jwt;
        try {
            jwt = decoder.decode(token);
        } catch (JwtException e) {
            throw new InvalidHandoffException("Invalid or expired lobby handoff token.", e);
        }

        Map<String, Object> raw = jwt.getClaim("handoff");
        if (raw == null) {
            throw new InvalidHandoffException("The token does not contain a lobby handoff.");
        }

        HandoffClaims claims;
        try {
            claims = objectMapper.convertValue(raw, HandoffClaims.class);
        } catch (RuntimeException e) {
            throw new InvalidHandoffException("The lobby handoff could not be read.", e);
        }

        List<PlayerClaims> players = claims.players() == null ? List.of() : claims.players();
        List<TeamClaims> teams = claims.teams() == null ? List.of() : claims.teams();

        return new LobbyHandoff(
                claims.lobbyCode(),
                players.stream()
                        .map(p -> new LobbyHandoff.Player(p.playerId(), p.username(), p.teamId()))
                        .toList(),
                teams.stream()
                        .map(t -> new LobbyHandoff.Team(t.teamId(), t.name(), t.colour()))
                        .toList());
    }

    private static RSAPublicKey readPublicKey(LobbyProperties properties) throws Exception {
        String pem = properties.publicKeyLocation().getContentAsString(StandardCharsets.UTF_8)
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
        return (RSAPublicKey) KeyFactory.getInstance("RSA")
                .generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(pem)));
    }

    // ---- Mirrors the lobby's HandoffPayload JSON (only the fields we use) ----
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record HandoffClaims(String lobbyCode, List<PlayerClaims> players, List<TeamClaims> teams) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PlayerClaims(UUID playerId, String username, Integer teamId) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TeamClaims(int teamId, String name, String colour) {}
}