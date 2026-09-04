package dev.tanguy.game.jeopardy.gameplay.port.in.session;

import dev.tanguy.game.jeopardy.common.domain.model.id.GameSessionId;

public interface CreateGameSessionUseCase {

    GameSessionId createGameSession(CreateGameSessionCommand command);
}