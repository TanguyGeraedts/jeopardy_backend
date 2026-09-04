package dev.tanguy.game.jeopardy.gameplay.adapter.out.persistence;

import dev.tanguy.game.jeopardy.common.domain.model.id.GameSessionId;
import dev.tanguy.game.jeopardy.gameplay.domain.model.GameSession;
import dev.tanguy.game.jeopardy.gameplay.port.out.session.DeleteGameSessionPort;
import dev.tanguy.game.jeopardy.gameplay.port.out.session.LoadGameSessionPort;
import dev.tanguy.game.jeopardy.gameplay.port.out.session.SaveGameSessionPort;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryGameSessionRepositoryAdapter implements LoadGameSessionPort, SaveGameSessionPort, DeleteGameSessionPort {

    private final Map<GameSessionId, GameSession> storage = new ConcurrentHashMap<>();

    @Override
    public Optional<GameSession> loadGameSessionById(GameSessionId id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public void saveGameSession(GameSession session) {
        storage.put(session.getId(), session);
    }

    @Override
    public void deleteGameSessionById(GameSessionId id) {
        storage.remove(id);
    }
}