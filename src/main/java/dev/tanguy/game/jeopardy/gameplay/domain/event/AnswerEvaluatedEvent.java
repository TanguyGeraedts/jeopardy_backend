package dev.tanguy.game.jeopardy.gameplay.domain.event;

import dev.tanguy.game.jeopardy.common.domain.model.id.GameSessionId;
import dev.tanguy.game.jeopardy.common.domain.model.id.PlayerId;
import dev.tanguy.game.jeopardy.common.domain.model.id.TeamId;
import dev.tanguy.game.jeopardy.common.events.DomainEvent;

import java.time.Instant;

public record AnswerEvaluatedEvent(
        GameSessionId sessionId,
        PlayerId playerId,
        TeamId teamId,
        boolean isCorrect,
        int points,
        Instant occurredAt
) implements DomainEvent {
    public AnswerEvaluatedEvent(GameSessionId sessionId, PlayerId playerId, TeamId teamId, boolean isCorrect, int points) {
        this(sessionId, playerId, teamId, isCorrect, points, Instant.now());
    }
}