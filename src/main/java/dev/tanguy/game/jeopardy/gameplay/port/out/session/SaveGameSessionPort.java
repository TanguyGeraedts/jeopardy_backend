package dev.tanguy.game.jeopardy.gameplay.port.out.session;

import dev.tanguy.game.jeopardy.gameplay.domain.model.GameSession;

public interface SaveGameSessionPort {

    void saveGameSession(GameSession session);
}