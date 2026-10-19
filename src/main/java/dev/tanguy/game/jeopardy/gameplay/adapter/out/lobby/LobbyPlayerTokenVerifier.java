package dev.tanguy.game.jeopardy.gameplay.adapter.out.lobby;

import dev.tanguy.game.jeopardy.gameplay.domain.exception.player.InvalidPlayerTokenException;
import dev.tanguy.game.jeopardy.gameplay.port.out.lobby.LobbyPlayerIdentity;
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

import java.util.List;
import java.util.UUID;

/**
 * Verifies the per-player game token the lobby gives each player when the game starts:
 * RS256 with the lobby's key, issuer, audience ("game-player") and expiry. sub = playerId.
 * Separate from LobbyHandoffVerifier so a handoff token can never be used as a player token (or the other way round).
 */
@Component
public class LobbyPlayerTokenVerifier {

    private final JwtDecoder decoder;

    public LobbyPlayerTokenVerifier(LobbyProperties properties) throws Exception {
        NimbusJwtDecoder nimbus = NimbusJwtDecoder.withPublicKey(LobbyHandoffVerifier.readPublicKey(properties)).build();
        OAuth2TokenValidator<Jwt> audience = new JwtClaimValidator<List<String>>(
                JwtClaimNames.AUD, aud -> aud != null && aud.contains(properties.playerAudience()));
        nimbus.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefaultWithIssuer(properties.issuer()), audience));
        this.decoder = nimbus;
    }

    public LobbyPlayerIdentity verify(String token) {
        if (token == null || token.isBlank()) {
            throw new InvalidPlayerTokenException("A player token is required.");
        }

        Jwt jwt;
        try {
            jwt = decoder.decode(token);
        } catch (JwtException e) {
            throw new InvalidPlayerTokenException("Invalid or expired player token.", e);
        }

        String lobbyCode = jwt.getClaimAsString("lobbyCode");
        if (lobbyCode == null || jwt.getSubject() == null) {
            throw new InvalidPlayerTokenException("The player token is incomplete.");
        }
        try {
            return new LobbyPlayerIdentity(lobbyCode, UUID.fromString(jwt.getSubject()));
        } catch (IllegalArgumentException e) {
            throw new InvalidPlayerTokenException("The player token has an invalid player id.", e);
        }
    }
}
