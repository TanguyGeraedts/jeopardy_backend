package dev.tanguy.game.jeopardy.creator.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Body for both creating and renaming a category. */
public record CategoryRequest(
        @NotBlank(message = "Category name is required")
        @Size(max = 255, message = "Category name must be at most 255 characters")
        String name
) {
    public CategoryRequest {
        name = name == null ? null : name.trim();
    }
}