package dev.tanguy.game.jeopardy.creator.port.in.category;

import dev.tanguy.game.jeopardy.creator.domain.model.Category;

public interface AddCategoryUseCase {

    Category addCategory(AddCategoryCommand command);
}