package dev.tanguy.game.jeopardy.gameplay.port.in.session;

import dev.tanguy.game.jeopardy.common.domain.model.id.ClueId;
import dev.tanguy.game.jeopardy.common.domain.model.id.GameSessionId;
import dev.tanguy.game.jeopardy.common.domain.model.id.OwnerId;

/** A typed answer for the open clue, judged by text match. Only the session owner may send it. */
public record AnswerClueCommand(
        GameSessionId sessionId,
        OwnerId requesterId,
        ClueId clueId,
        String answer
) {}
