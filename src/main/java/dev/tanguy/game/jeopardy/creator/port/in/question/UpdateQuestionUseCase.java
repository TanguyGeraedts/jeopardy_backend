package dev.tanguy.game.jeopardy.creator.port.in.question;

import dev.tanguy.game.jeopardy.creator.domain.model.Question;

public interface UpdateQuestionUseCase {

    Question updateQuestion(UpdateQuestionCommand command);
}