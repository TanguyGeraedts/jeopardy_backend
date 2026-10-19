package dev.tanguy.game.jeopardy.gameplay.domain.event;

import dev.tanguy.game.jeopardy.common.domain.model.id.ClueId;
import dev.tanguy.game.jeopardy.common.domain.model.id.GameSessionId;
import dev.tanguy.game.jeopardy.common.events.DomainEvent;

import java.time.Instant;

/** The host opened a clue. */
public record ClueSelectedEvent(
        GameSessionId sessionId,
        ClueId clueId,
        Instant occurredAt
) implements DomainEvent {
    public ClueSelectedEvent(GameSessionId sessionId, ClueId clueId) {
        this(sessionId, clueId, Instant.now());
    }
}
