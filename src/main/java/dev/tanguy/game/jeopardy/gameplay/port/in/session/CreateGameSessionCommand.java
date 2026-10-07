package dev.tanguy.game.jeopardy.gameplay.port.in.session;

import dev.tanguy.game.jeopardy.common.domain.model.id.OwnerId;
import dev.tanguy.game.jeopardy.common.domain.model.id.QuizId;
import dev.tanguy.game.jeopardy.gameplay.domain.model.GameMode;

import java.util.Objects;

public record CreateGameSessionCommand(
        QuizId quizId,
        OwnerId requesterId,
        GameMode mode
) {
    public CreateGameSessionCommand {
        Objects.requireNonNull(quizId, "quizId cannot be null");
        Objects.requireNonNull(requesterId, "requesterId cannot be null");
        Objects.requireNonNull(mode, "mode cannot be null");
    }
}