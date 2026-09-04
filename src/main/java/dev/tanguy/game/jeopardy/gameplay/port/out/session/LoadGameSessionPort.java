package dev.tanguy.game.jeopardy.gameplay.port.out.session;

import dev.tanguy.game.jeopardy.common.domain.model.id.GameSessionId;
import dev.tanguy.game.jeopardy.gameplay.domain.model.GameSession;

import java.util.Optional;

public interface LoadGameSessionPort {

    Optional<GameSession> loadGameSessionById(GameSessionId id);
}