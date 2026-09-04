package dev.tanguy.game.jeopardy.gameplay.domain.event;

import dev.tanguy.game.jeopardy.common.domain.model.id.GameSessionId;
import dev.tanguy.game.jeopardy.common.events.DomainEvent;
import dev.tanguy.game.jeopardy.gameplay.domain.model.GameState;

import java.time.Instant;

public record GameStateChangedEvent(
        GameSessionId sessionId,
        GameState newState,
        Instant occurredAt
) implements DomainEvent {
    public GameStateChangedEvent(GameSessionId sessionId, GameState newState) {
        this(sessionId, newState, Instant.now());
    }
}