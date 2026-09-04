package dev.tanguy.game.jeopardy.gameplay.domain.event;

import dev.tanguy.game.jeopardy.common.domain.model.id.GameSessionId;
import dev.tanguy.game.jeopardy.common.events.DomainEvent;

import java.time.Instant;

public record GameSessionCreatedEvent(
        GameSessionId sessionId,
        Instant occurredAt
) implements DomainEvent {
    public GameSessionCreatedEvent(GameSessionId sessionId) {
        this(sessionId, Instant.now());
    }
}