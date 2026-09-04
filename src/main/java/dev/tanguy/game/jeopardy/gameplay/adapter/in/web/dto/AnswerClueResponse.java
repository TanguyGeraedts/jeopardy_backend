package dev.tanguy.game.jeopardy.gameplay.adapter.in.web.dto;

public record AnswerClueResponse(
        boolean correct,
        int scoreDelta,
        String expectedAnswer
) {}