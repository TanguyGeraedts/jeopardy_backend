package dev.tanguy.game.jeopardy.creator.domain.event.category;

import dev.tanguy.game.jeopardy.common.domain.exception.DomainConflictException;
import dev.tanguy.game.jeopardy.common.domain.model.id.CategoryId;

public class DuplicateQuestionPointsException extends DomainConflictException {

    public DuplicateQuestionPointsException(CategoryId categoryId, int points) {
        super("Category with ID " + categoryId + " already contains a question worth " + points + " points.");
    }
}