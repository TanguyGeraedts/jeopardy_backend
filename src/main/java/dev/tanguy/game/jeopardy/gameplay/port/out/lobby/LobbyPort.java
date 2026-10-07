package dev.tanguy.game.jeopardy.gameplay.port.out.lobby;

import dev.tanguy.game.jeopardy.common.domain.model.id.GameSessionId;
import dev.tanguy.game.jeopardy.gameplay.domain.model.GameMode;

public interface LobbyPort {

    /** Creates the room in the lobby microservice. Throws ExternalServiceException if the lobby can't be reached. */
    LobbyInfo createLobby(CreateLobbyRequest request);

    /** Best-effort cleanup. Must never throw. */
    void closeLobby(String lobbyCode);

    /** Checks the lobby's signed handoff token and returns its content. Throws InvalidHandoffException if bad. */
    LobbyHandoff verifyHandoff(String handoffToken);

    /** maxPlayers = lobby seats (the quiz master does not join the lobby). teamCount is null for SOLO. */
    record CreateLobbyRequest(GameSessionId gameSessionId, GameMode mode, int maxPlayers, Integer teamCount) {}

    record LobbyInfo(String code) {}
}