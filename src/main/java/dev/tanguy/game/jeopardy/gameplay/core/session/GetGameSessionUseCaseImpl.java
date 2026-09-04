package dev.tanguy.game.jeopardy.gameplay.core.session;

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
        return loadGameSessionPort.loadGameSessionById(query.gameSessionId())
                .orElseThrow(() -> new GameSessionNotFoundException(query.gameSessionId()));
    }
}