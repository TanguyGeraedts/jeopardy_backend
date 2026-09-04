package dev.tanguy.game.jeopardy.gameplay.domain.event;

import dev.tanguy.game.jeopardy.common.domain.model.id.ClueId;
import dev.tanguy.game.jeopardy.common.domain.model.id.GameSessionId;
import dev.tanguy.game.jeopardy.common.domain.model.id.PlayerId;
import dev.tanguy.game.jeopardy.common.events.DomainEvent;

import java.time.Instant;

public record ClueSelectedEvent(
        GameSessionId sessionId,
        PlayerId selectorId,
        ClueId clueId,
        Instant occurredAt
) implements DomainEvent {
    public ClueSelectedEvent(GameSessionId sessionId, PlayerId selectorId, ClueId clueId) {
        this(sessionId, selectorId, clueId, Instant.now());
    }
}