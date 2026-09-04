package dev.tanguy.game.jeopardy.gameplay.port.in.session;

import dev.tanguy.game.jeopardy.common.domain.model.id.GameSessionId;

import java.util.Objects;

public record GetGameSessionQuery(
        GameSessionId gameSessionId
) {
    public GetGameSessionQuery {
        Objects.requireNonNull(gameSessionId, "GameSessionId cannot be null");
    }
}
