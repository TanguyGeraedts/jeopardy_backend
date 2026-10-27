package dev.tanguy.game.jeopardy.gameplay.adapter.in.web.dto;

import dev.tanguy.game.jeopardy.gameplay.domain.model.GameMode;
import dev.tanguy.game.jeopardy.gameplay.domain.model.TurnRules;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateGameSessionRequest(
        @NotNull(message = "Quiz ID is required")
        UUID quizId,
        @NotNull(message = "Game mode is required (SOLO or TEAM)")
        GameMode mode,
        @Min(value = 1, message = "maxPlayers must be at least 1")
        @Max(value = 50, message = "maxPlayers can be at most 50")
        Integer maxPlayers,
        @Min(value = 2, message = "teamCount must be at least 2")
        @Max(value = 8, message = "teamCount can be at most 8")
        Integer teamCount,
        /** Taking turns: each clue one team gets a head start to buzz. Default off. */
        Boolean turns,
        @Min(value = TurnRules.MIN_HEAD_START, message = "The head start must be at least 5 seconds")
        @Max(value = TurnRules.MAX_HEAD_START, message = "The head start can be at most 10 seconds")
        Integer headStartSeconds
) {}