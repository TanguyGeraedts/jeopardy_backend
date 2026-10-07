package dev.tanguy.game.jeopardy.gameplay.adapter.out.persistence;

import dev.tanguy.game.jeopardy.common.domain.model.id.GameSessionId;
import dev.tanguy.game.jeopardy.gameplay.domain.model.GameSession;
import dev.tanguy.game.jeopardy.gameplay.port.out.session.DeleteGameSessionPort;
import dev.tanguy.game.jeopardy.gameplay.port.out.session.LoadGameSessionPort;
import dev.tanguy.game.jeopardy.gameplay.port.out.session.SaveGameSessionPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class GameSessionAdapter implements LoadGameSessionPort, SaveGameSessionPort, DeleteGameSessionPort {

    private final GameSessionJpaRepository repository;
    private final GameSessionMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public Optional<GameSession> loadGameSessionById(GameSessionId id) {
        // Map inside the transaction: the child collections are lazy.
        return repository.findById(id.value()).map(mapper::toDomain);
    }

    @Override
    @Transactional
    public void saveGameSession(GameSession session) {
        repository.save(mapper.toJpaEntity(session));
    }

    @Override
    @Transactional
    public void deleteGameSessionById(GameSessionId id) {
        repository.deleteById(id.value());
    }
}