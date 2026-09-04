package dev.tanguy.game.jeopardy.gameplay.adapter.in.web.dto;

import java.util.UUID;

public record CreateGameSessionResponse(
        UUID gameSessionId
) {}