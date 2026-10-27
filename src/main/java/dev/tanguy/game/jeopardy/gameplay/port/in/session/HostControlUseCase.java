package dev.tanguy.game.jeopardy.gameplay.port.in.session;

import dev.tanguy.game.jeopardy.common.domain.model.id.ClueId;
import dev.tanguy.game.jeopardy.common.domain.model.id.GameSessionId;
import dev.tanguy.game.jeopardy.common.domain.model.id.OwnerId;
import dev.tanguy.game.jeopardy.common.domain.model.id.TeamId;
import dev.tanguy.game.jeopardy.gameplay.domain.model.BuzzerSound;
import dev.tanguy.game.jeopardy.gameplay.domain.model.GameSession;
import dev.tanguy.game.jeopardy.gameplay.domain.model.TurnRules;

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

    /** Stop early: straight to GAME_OVER (the final scoreboard) with the scores as they are. */
    GameSession endGame(GameSessionId sessionId, OwnerId requesterId);

    /** Taking turns on/off and the head start. Any time. */
    GameSession changeTurnRules(GameSessionId sessionId, OwnerId requesterId, TurnRules rules);

    /** Hands the turn to a team; the rotation continues from there. */
    GameSession giveTurn(GameSessionId sessionId, OwnerId requesterId, TeamId teamId);

    /** The turn moves on to the next team. */
    GameSession skipTurn(GameSessionId sessionId, OwnerId requesterId);

    /** Exactly one of delta / score is used: delta adds, score sets. */
    GameSession changeScore(GameSessionId sessionId, OwnerId requesterId, TeamId teamId, Integer delta, Integer score);

    /** sound null = back to automatic. */
    GameSession setTeamBuzzerSound(GameSessionId sessionId, OwnerId requesterId, TeamId teamId, BuzzerSound sound);
}
