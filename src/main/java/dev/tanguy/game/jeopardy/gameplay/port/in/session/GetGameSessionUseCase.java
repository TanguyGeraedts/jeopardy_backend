package dev.tanguy.game.jeopardy.gameplay.port.in.session;

import dev.tanguy.game.jeopardy.gameplay.domain.model.GameSession;


public interface GetGameSessionUseCase {
    GameSession getGameSession(GetGameSessionQuery query);
}