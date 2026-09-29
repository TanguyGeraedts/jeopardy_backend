package dev.tanguy.game.jeopardy.creator.domain.event.quiz;

import dev.tanguy.game.jeopardy.common.domain.exception.NotFoundException;
import dev.tanguy.game.jeopardy.common.domain.model.id.QuizId;

public class QuizNotFoundException extends NotFoundException {

    public QuizNotFoundException(QuizId quizId) {
        super("Quiz with ID " + quizId.value() + " was not found.");
    }
}