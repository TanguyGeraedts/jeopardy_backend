package dev.tanguy.game.jeopardy.gameplay.port.in.session;

import dev.tanguy.game.jeopardy.common.domain.model.id.GameSessionId;

import java.util.Objects;

public record StartGameFromLobbyCommand(GameSessionId sessionId, String handoffToken) {

    public StartGameFromLobbyCommand {
        Objects.requireNonNull(sessionId, "sessionId cannot be null");
        if (handoffToken == null || handoffToken.isBlank()) {
            throw new IllegalArgumentException("handoffToken cannot be blank");
        }
    }
}