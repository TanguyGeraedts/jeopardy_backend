package dev.tanguy.game.jeopardy.gameplay.domain.event;

import dev.tanguy.game.jeopardy.common.domain.model.id.GameSessionId;
import dev.tanguy.game.jeopardy.common.domain.model.id.PlayerId;
import dev.tanguy.game.jeopardy.common.events.DomainEvent;

import java.time.Instant;

public record BuzzerPressedEvent(
        GameSessionId sessionId,
        PlayerId playerId,
        Instant occurredAt
) implements DomainEvent {
    public BuzzerPressedEvent(GameSessionId sessionId, PlayerId playerId) {
        this(sessionId, playerId, Instant.now());
    }
}