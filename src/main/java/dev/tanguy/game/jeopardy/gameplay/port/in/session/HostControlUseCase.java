package dev.tanguy.game.jeopardy.gameplay.port.in.session;

import dev.tanguy.game.jeopardy.common.domain.model.id.ClueId;
import dev.tanguy.game.jeopardy.common.domain.model.id.GameSessionId;
import dev.tanguy.game.jeopardy.common.domain.model.id.OwnerId;
import dev.tanguy.game.jeopardy.common.domain.model.id.TeamId;
import dev.tanguy.game.jeopardy.gameplay.domain.model.GameSession;

/**
 * What the host (the session owner) does during a game. Grouped because each is a one-liner on GameSession.
 * Every method throws GameSessionNotFoundException if the requester doesn't own the session,
 * and returns the session as saved.
 */
public interface HostControlUseCase {

    GameSession selectClue(GameSessionId sessionId, OwnerId requesterId, ClueId clueId);

    GameSession openBuzzers(GameSessionId sessionId, OwnerId requesterId);

    GameSession judgeAnswer(GameSessionId sessionId, OwnerId requesterId, boolean correct);

    GameSession closeClue(GameSessionId sessionId, OwnerId requesterId);

    /** Exactly one of delta / score is used: delta adds, score sets. */
    GameSession changeScore(GameSessionId sessionId, OwnerId requesterId, TeamId teamId, Integer delta, Integer score);
}
