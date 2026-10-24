package dev.tanguy.game.jeopardy.gameplay.port.in.session;

import dev.tanguy.game.jeopardy.common.domain.model.id.GameSessionId;
import dev.tanguy.game.jeopardy.common.domain.model.id.OwnerId;

import java.util.Objects;

/** Reads the full session (with answers): only its owner may. */
public record GetGameSessionQuery(
        GameSessionId gameSessionId,
        OwnerId requesterId
) {
    public GetGameSessionQuery {
        Objects.requireNonNull(gameSessionId, "GameSessionId cannot be null");
        Objects.requireNonNull(requesterId, "RequesterId cannot be null");
    }
}
