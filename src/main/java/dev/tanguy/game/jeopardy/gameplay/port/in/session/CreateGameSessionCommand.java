// gameplay/port/in/session/CreateGameSessionCommand.java
package dev.tanguy.game.jeopardy.gameplay.port.in.session;

import dev.tanguy.game.jeopardy.common.domain.model.id.OwnerId;
import dev.tanguy.game.jeopardy.common.domain.model.id.QuizId;
import dev.tanguy.game.jeopardy.gameplay.domain.model.GameMode;

import java.util.Objects;

public record CreateGameSessionCommand(
        QuizId quizId,
        OwnerId requesterId,
        GameMode mode,
        int maxPlayers,
        /** Number of teams: required for TEAM games, must be null for SOLO games. */
        Integer teamCount
) {
    public static final int DEFAULT_MAX_PLAYERS = 8;

    public CreateGameSessionCommand {
        Objects.requireNonNull(quizId, "quizId cannot be null");
        Objects.requireNonNull(requesterId, "requesterId cannot be null");
        Objects.requireNonNull(mode, "mode cannot be null");
        if (maxPlayers < 1) {
            throw new IllegalArgumentException("maxPlayers must be at least 1");
        }
        if (mode == GameMode.TEAM && (teamCount == null || teamCount < 2)) {
            throw new IllegalArgumentException("A team game needs at least 2 teams (teamCount).");
        }
        if (mode == GameMode.SOLO && teamCount != null) {
            throw new IllegalArgumentException("A solo game cannot have teams (teamCount must be empty).");
        }
    }
}