package dev.tanguy.game.jeopardy.gameplay.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record AnswerClueRequest(
        @NotBlank(message = "Clue ID is required")
        String clueId,
        @NotBlank(message = "Answer is required")
        String answer
) {}