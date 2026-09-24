package dev.tanguy.game.jeopardy.creator.domain.event.category;

import dev.tanguy.game.jeopardy.common.domain.exception.DomainConflictException;
import dev.tanguy.game.jeopardy.common.domain.model.id.CategoryId;

public class InvalidCategoryNameException extends DomainConflictException {

    public InvalidCategoryNameException(CategoryId categoryId) {
        super("Category with ID " + categoryId + " must have a non-blank name.");
    }

    public InvalidCategoryNameException(CategoryId categoryId, String name) {
        super("Category with ID " + categoryId + " has an invalid name: '" + name + "'");
    }
}