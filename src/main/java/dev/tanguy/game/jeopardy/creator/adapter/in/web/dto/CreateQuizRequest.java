package dev.tanguy.game.jeopardy.creator.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateQuizRequest(
        @NotBlank(message = "Quiz name is required")
        @Size(max = 255, message = "Quiz name must be at most 255 characters")
        String name
) {}