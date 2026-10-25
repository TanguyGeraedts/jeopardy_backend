package dev.tanguy.game.jeopardy.gameplay.port.out.board;

import dev.tanguy.game.jeopardy.common.domain.model.id.OwnerId;
import dev.tanguy.game.jeopardy.common.domain.model.id.QuizId;
import dev.tanguy.game.jeopardy.gameplay.domain.model.ClueState;

import java.util.List;

public interface LoadBoardTemplatePort {

    /** Copies the quiz into fresh clues. Fails unless the quiz is the requester's own or published. */
    List<ClueState> loadCluesForQuiz(QuizId quizId, OwnerId requesterId);
}