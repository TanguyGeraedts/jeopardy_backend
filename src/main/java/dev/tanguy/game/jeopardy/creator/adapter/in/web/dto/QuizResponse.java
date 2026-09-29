package dev.tanguy.game.jeopardy.creator.adapter.in.web.dto;

import java.util.UUID;

public record QuizResponse(
        UUID id,
        String name
) {}