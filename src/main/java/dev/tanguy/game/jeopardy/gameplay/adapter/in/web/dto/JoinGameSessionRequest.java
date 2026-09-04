package dev.tanguy.game.jeopardy.gameplay.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record JoinGameSessionRequest(
        @NotNull(message = "Player ID is required")
        UUID playerId,
        @NotBlank(message = "Player name is required")
        String playerName,
        String externalTeamId,
        String teamName,
        String teamColour
) {}