package dev.tanguy.game.jeopardy.gameplay.core.session;

import dev.tanguy.game.jeopardy.common.domain.model.id.GameSessionId;
import dev.tanguy.game.jeopardy.gameplay.domain.exception.session.GameSessionNotFoundException;
import dev.tanguy.game.jeopardy.gameplay.domain.model.GameSession;
import dev.tanguy.game.jeopardy.gameplay.port.in.session.GetGameSessionQuery;
import dev.tanguy.game.jeopardy.gameplay.port.in.session.GetGameSessionUseCase;
import dev.tanguy.game.jeopardy.gameplay.port.out.session.LoadGameSessionPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetGameSessionUseCaseImpl implements GetGameSessionUseCase {

    private final LoadGameSessionPort loadGameSessionPort;

    @Override
    public GameSession getGameSession(GetGameSessionQuery query) {
        // Same rule as host actions: another user's session looks exactly like a missing one.
        return loadGameSessionPort.loadGameSessionById(query.gameSessionId())
                .filter(session -> session.getOwnerId().equals(query.requesterId()))
                .orElseThrow(() -> new GameSessionNotFoundException(query.gameSessionId()));
    }

    @Override
    public GameSession getGameSessionForLiveView(GameSessionId gameSessionId) {
        return loadGameSessionPort.loadGameSessionById(gameSessionId)
                .orElseThrow(() -> new GameSessionNotFoundException(gameSessionId));
    }
}
