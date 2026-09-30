package dev.tanguy.game.jeopardy.creator.adapter.in.web;

import dev.tanguy.game.jeopardy.common.domain.model.id.OwnerId;
import dev.tanguy.game.jeopardy.common.infrastructure.security.CurrentUser;
import dev.tanguy.game.jeopardy.common.web.ApiPaths;
import dev.tanguy.game.jeopardy.common.web.ApiResponse;
import dev.tanguy.game.jeopardy.creator.adapter.in.web.dto.CategoryRequest;
import dev.tanguy.game.jeopardy.creator.adapter.in.web.dto.QuizResponse.CategoryResponse;
import dev.tanguy.game.jeopardy.creator.adapter.in.web.mapper.CategoryWebMapper;
import dev.tanguy.game.jeopardy.creator.adapter.in.web.mapper.QuizWebMapper;
import dev.tanguy.game.jeopardy.creator.domain.model.Category;
import dev.tanguy.game.jeopardy.creator.port.in.category.AddCategoryUseCase;
import dev.tanguy.game.jeopardy.creator.port.in.category.RemoveCategoryUseCase;
import dev.tanguy.game.jeopardy.creator.port.in.category.RenameCategoryUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping(ApiPaths.Creator.BASE)
@RequiredArgsConstructor
public class CategoryController {

    /// Auth
    private final CurrentUser currentUser;

    /// Use cases
    private final AddCategoryUseCase addCategoryUseCase;
    private final RenameCategoryUseCase renameCategoryUseCase;
    private final RemoveCategoryUseCase removeCategoryUseCase;

    @PostMapping(ApiPaths.Creator.CATEGORIES)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CategoryResponse>> addCategory(
            @PathVariable UUID id,
            @Valid @RequestBody CategoryRequest request) {
        OwnerId requesterId = currentUser.require().ownerId();

        Category category = addCategoryUseCase.addCategory(CategoryWebMapper.toAddCommand(id, request, requesterId));

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(QuizWebMapper.toCategoryResponse(category), "Category created successfully"));
    }

    @PutMapping(ApiPaths.Creator.CATEGORY_BY_ID)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CategoryResponse>> renameCategory(
            @PathVariable UUID id,
            @PathVariable UUID categoryId,
            @Valid @RequestBody CategoryRequest request) {
        OwnerId requesterId = currentUser.require().ownerId();

        Category category = renameCategoryUseCase.renameCategory(
                CategoryWebMapper.toRenameCommand(id, categoryId, request, requesterId));

        return ResponseEntity.ok(
                ApiResponse.success(QuizWebMapper.toCategoryResponse(category), "Category updated successfully"));
    }

    @DeleteMapping(ApiPaths.Creator.CATEGORY_BY_ID)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> removeCategory(@PathVariable UUID id, @PathVariable UUID categoryId) {
        OwnerId requesterId = currentUser.require().ownerId();

        removeCategoryUseCase.removeCategory(CategoryWebMapper.toRemoveCommand(id, categoryId, requesterId));

        return ResponseEntity.noContent().build();
    }
}