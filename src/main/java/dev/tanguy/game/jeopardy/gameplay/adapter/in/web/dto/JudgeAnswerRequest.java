package dev.tanguy.game.jeopardy.gameplay.adapter.in.web.dto;

import jakarta.validation.constraints.NotNull;

public record JudgeAnswerRequest(
        @NotNull(message = "correct is required")
        Boolean correct
) {}
