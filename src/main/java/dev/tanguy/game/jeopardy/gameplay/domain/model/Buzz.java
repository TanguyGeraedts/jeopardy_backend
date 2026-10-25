package dev.tanguy.game.jeopardy.gameplay.domain.model;

import dev.tanguy.game.jeopardy.common.domain.model.id.PlayerId;

import java.time.Instant;
import java.util.Objects;

/** One buzz on the open clue. at = when the server received it (before waiting for the session lock). */
public record Buzz(PlayerId playerId, Instant at) {
    public Buzz {
        Objects.requireNonNull(playerId, "playerId cannot be null");
        Objects.requireNonNull(at, "at cannot be null");
    }
}
