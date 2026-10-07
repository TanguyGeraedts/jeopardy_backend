package dev.tanguy.game.jeopardy.gameplay.port.out.board;

import dev.tanguy.game.jeopardy.common.domain.model.id.OwnerId;
import dev.tanguy.game.jeopardy.common.domain.model.id.QuizId;
import dev.tanguy.game.jeopardy.gameplay.domain.model.ClueState;

import java.util.List;

public interface LoadBoardTemplatePort {

    /** Copies the quiz into fresh clues. Fails if the requester doesn't own the quiz. */
    List<ClueState> loadCluesForQuiz(QuizId quizId, OwnerId requesterId);
}