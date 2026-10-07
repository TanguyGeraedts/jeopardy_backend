package dev.tanguy.game.jeopardy.gameplay.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record LobbyHandoffRequest(
        @NotBlank(message = "Handoff token is required")
        String token
) {}