package dev.tanguy.game.jeopardy.gameplay.port.in.session;

import dev.tanguy.game.jeopardy.common.domain.model.id.GameSessionId;
import dev.tanguy.game.jeopardy.gameplay.domain.model.GameSession;


public interface GetGameSessionUseCase {

    /** Owner only. Missing and "not yours" both throw GameSessionNotFoundException. */
    GameSession getGameSession(GetGameSessionQuery query);

    /**
     * No owner check: only for the public live view, which must be mapped without answers
     * (GameSessionResponseMapper.toPublicResponse). Never return this session to a client as-is.
     */
    GameSession getGameSessionForLiveView(GameSessionId gameSessionId);
}
