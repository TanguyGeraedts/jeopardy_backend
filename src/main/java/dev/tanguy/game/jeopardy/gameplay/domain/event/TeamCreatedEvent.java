package dev.tanguy.game.jeopardy.gameplay.domain.event;

import dev.tanguy.game.jeopardy.common.domain.model.id.GameSessionId;
import dev.tanguy.game.jeopardy.common.domain.model.id.TeamId;
import dev.tanguy.game.jeopardy.common.events.DomainEvent;

import java.time.Instant;

public record TeamCreatedEvent(
        GameSessionId sessionId,
        TeamId teamId,
        String teamName,
        Instant occurredAt
) implements DomainEvent {
    public TeamCreatedEvent(GameSessionId sessionId, TeamId teamId, String teamName) {
        this(sessionId, teamId, teamName, Instant.now());
    }
}