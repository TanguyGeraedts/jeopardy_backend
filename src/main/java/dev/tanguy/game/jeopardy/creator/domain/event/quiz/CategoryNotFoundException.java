package dev.tanguy.game.jeopardy.creator.domain.event.quiz;

import dev.tanguy.game.jeopardy.common.domain.exception.DomainConflictException;
import dev.tanguy.game.jeopardy.common.domain.model.id.CategoryId;
import dev.tanguy.game.jeopardy.common.domain.model.id.QuizId;

public class CategoryNotFoundException extends DomainConflictException {

    public CategoryNotFoundException(QuizId quizId, CategoryId categoryId) {
        super("Category with ID " + categoryId + " was not found in Quiz " + quizId);
    }
}