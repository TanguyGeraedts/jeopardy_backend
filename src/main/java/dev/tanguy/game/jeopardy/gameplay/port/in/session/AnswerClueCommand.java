package dev.tanguy.game.jeopardy.gameplay.port.in.session;

import dev.tanguy.game.jeopardy.common.domain.model.id.ClueId;
import dev.tanguy.game.jeopardy.common.domain.model.id.GameSessionId;

public record AnswerClueCommand(
        GameSessionId sessionId,
        ClueId clueId,
        String answer
) {}
