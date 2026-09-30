package dev.tanguy.game.jeopardy.creator.adapter.in.web.mapper;

import dev.tanguy.game.jeopardy.common.domain.model.id.CategoryId;
import dev.tanguy.game.jeopardy.common.domain.model.id.OwnerId;
import dev.tanguy.game.jeopardy.common.domain.model.id.QuizId;
import dev.tanguy.game.jeopardy.creator.adapter.in.web.dto.CategoryRequest;
import dev.tanguy.game.jeopardy.creator.port.in.category.AddCategoryCommand;
import dev.tanguy.game.jeopardy.creator.port.in.category.RemoveCategoryCommand;
import dev.tanguy.game.jeopardy.creator.port.in.category.RenameCategoryCommand;

import java.util.UUID;

public final class CategoryWebMapper {

    private CategoryWebMapper() {}

    public static AddCategoryCommand toAddCommand(UUID quizId, CategoryRequest request, OwnerId requesterId) {
        return new AddCategoryCommand(QuizId.of(quizId.toString()), requesterId, request.name());
    }

    public static RenameCategoryCommand toRenameCommand(
            UUID quizId, UUID categoryId, CategoryRequest request, OwnerId requesterId) {
        return new RenameCategoryCommand(
                QuizId.of(quizId.toString()),
                CategoryId.of(categoryId.toString()),
                requesterId,
                request.name());
    }

    public static RemoveCategoryCommand toRemoveCommand(UUID quizId, UUID categoryId, OwnerId requesterId) {
        return new RemoveCategoryCommand(
                QuizId.of(quizId.toString()),
                CategoryId.of(categoryId.toString()),
                requesterId);
    }
}