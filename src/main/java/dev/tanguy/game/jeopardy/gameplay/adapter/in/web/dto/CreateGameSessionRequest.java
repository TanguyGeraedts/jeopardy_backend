package dev.tanguy.game.jeopardy.gameplay.adapter.in.web.dto;

import dev.tanguy.game.jeopardy.gameplay.domain.model.GameMode;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateGameSessionRequest(
        @NotNull(message = "Quiz ID is required")
        UUID quizId,
        @NotNull(message = "Game mode is required (SOLO or TEAM)")
        GameMode mode
) {}