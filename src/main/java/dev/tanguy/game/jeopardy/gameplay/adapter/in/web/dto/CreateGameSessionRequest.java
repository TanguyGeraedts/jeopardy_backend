package dev.tanguy.game.jeopardy.gameplay.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateGameSessionRequest(
        @NotBlank(message = "Board ID is required")
        String boardId,
        boolean teamGame
) {}