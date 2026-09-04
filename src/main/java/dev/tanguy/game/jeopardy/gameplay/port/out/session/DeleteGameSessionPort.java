package dev.tanguy.game.jeopardy.gameplay.port.out.session;

import dev.tanguy.game.jeopardy.common.domain.model.id.GameSessionId;

public interface DeleteGameSessionPort {

    void deleteGameSessionById(GameSessionId id);
}