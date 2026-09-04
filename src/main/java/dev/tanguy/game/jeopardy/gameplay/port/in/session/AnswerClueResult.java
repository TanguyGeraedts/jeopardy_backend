package dev.tanguy.game.jeopardy.gameplay.port.in.session;

public record AnswerClueResult(
        boolean correct,
        int scoreDelta,
        String expectedAnswer
) {}
