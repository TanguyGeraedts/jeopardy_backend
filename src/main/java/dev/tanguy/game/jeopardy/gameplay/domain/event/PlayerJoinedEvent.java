package dev.tanguy.game.jeopardy.gameplay.domain.event;

import dev.tanguy.game.jeopardy.common.domain.model.id.GameSessionId;
import dev.tanguy.game.jeopardy.common.domain.model.id.PlayerId;
import dev.tanguy.game.jeopardy.common.domain.model.id.TeamId;
import dev.tanguy.game.jeopardy.common.events.DomainEvent;

import java.time.Instant;

public record PlayerJoinedEvent(
        GameSessionId sessionId,
        PlayerId playerId,
        TeamId teamId,
        String playerName,
        Instant occurredAt
) implements DomainEvent {
    public PlayerJoinedEvent(GameSessionId sessionId, PlayerId playerId, TeamId teamId, String playerName) {
        this(sessionId, playerId, teamId, playerName, Instant.now());
    }
}