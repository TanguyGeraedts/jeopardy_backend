package dev.tanguy.game.jeopardy.gameplay.adapter.out.lobby;

import dev.tanguy.game.jeopardy.common.domain.exception.ExternalServiceException;
import dev.tanguy.game.jeopardy.gameplay.port.out.lobby.LobbyHandoff;
import dev.tanguy.game.jeopardy.gameplay.port.out.lobby.LobbyPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class LobbyHttpAdapter implements LobbyPort {

    private final RestClient lobbyRestClient;
    private final LobbyProperties properties;
    private final LobbyHandoffVerifier handoffVerifier;

    // ---- Mirrors the lobby's LobbyConfig / GameInfo records (field names must match its JSON) ----
    public record GameInfoBody(
            String gameName, String gameBanner, String backgroundColor, String backgroundImage,
            String gameLaunchUrl, String handoffMethod, boolean passLobbyData) {}

    public record LobbyConfigBody(
            GameInfoBody gameInfo, String lobbyMode, Object characterConfig,
            int maxPlayers, int minPlayers, Integer teamCount, boolean balancedTeams,
            boolean allowLateJoin, int ttlSeconds, List<String> teamColours) {}

    /** The lobby returns the whole Lobby aggregate; we only need these two fields. */
    public record LobbyResponse(UUID id, String code) {}

    @Override
    public LobbyInfo createLobby(CreateLobbyRequest request) {
        String sessionId = request.gameSessionId().value().toString();

        LobbyConfigBody body = new LobbyConfigBody(
                new GameInfoBody(
                        properties.gameName(),
                        null,
                        properties.backgroundColor(),
                        null,
                        properties.launchUrl().replace("{gameSessionId}", sessionId),
                        "REDIRECT",
                        true),
                "BANNER",
                null,
                request.maxPlayers(),
                properties.minPlayers(),
                request.teamCount(),
                request.teamCount() != null && properties.balancedTeams(),
                false,
                (int) properties.ttl().toSeconds(),
                List.of());

        try {
            LobbyResponse response = lobbyRestClient.post()
                    .uri("/api/v1/lobbies")
                    .body(body)
                    .retrieve()
                    .body(LobbyResponse.class);

            if (response == null || response.code() == null || response.code().isBlank()) {
                throw new ExternalServiceException("The lobby service returned no lobby code.");
            }
            return new LobbyInfo(response.code());
        } catch (RestClientException e) {
            log.warn("Creating lobby for game session {} failed: {}", sessionId, e.getMessage());
            throw new ExternalServiceException("The lobby service is currently unavailable.", e);
        }
    }

    @Override
    public void closeLobby(String lobbyCode) {
        // The lobby has no delete/cancel endpoint yet; an unused lobby expires on its own after app.lobby.ttl.
        log.info("Lobby {} left to expire (no close endpoint on the lobby service)", lobbyCode);
    }

    @Override
    public LobbyHandoff verifyHandoff(String handoffToken) {
        return handoffVerifier.verify(handoffToken);
    }
}