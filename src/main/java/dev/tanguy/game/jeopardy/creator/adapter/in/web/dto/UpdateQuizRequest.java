package dev.tanguy.game.jeopardy.creator.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateQuizRequest(
        @NotBlank @Size(max = 255) String name
) {
    public UpdateQuizRequest {
        name = name == null ? null : name.trim();
    }
}