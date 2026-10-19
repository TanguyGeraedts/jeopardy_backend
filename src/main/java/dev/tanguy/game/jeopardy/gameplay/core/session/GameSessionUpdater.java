package dev.tanguy.game.jeopardy.gameplay.core.session;

import dev.tanguy.game.jeopardy.common.domain.model.id.GameSessionId;
import dev.tanguy.game.jeopardy.common.domain.model.id.OwnerId;
import dev.tanguy.game.jeopardy.common.events.DomainEventPublisher;
import dev.tanguy.game.jeopardy.gameplay.domain.exception.session.GameSessionNotFoundException;
import dev.tanguy.game.jeopardy.gameplay.domain.model.GameSession;
import dev.tanguy.game.jeopardy.gameplay.port.out.session.GameSessionUpdatesPort;
import dev.tanguy.game.jeopardy.gameplay.port.out.session.LoadGameSessionPort;
import dev.tanguy.game.jeopardy.gameplay.port.out.session.SaveGameSessionPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

/**
 * Load, change, save, publish, push live: one session at a time.
 * Changes to the same session are serialized in this JVM, so two buzzes arriving together can't
 * overwrite each other (the save replaces the whole aggregate). With more than one backend instance
 * this needs a database lock instead.
 */
@Component
@RequiredArgsConstructor
public class GameSessionUpdater {

    private final LoadGameSessionPort loadGameSessionPort;
    private final SaveGameSessionPort saveGameSessionPort;
    private final DomainEventPublisher domainEventPublisher;
    private final GameSessionUpdatesPort gameSessionUpdatesPort;

    private final Map<GameSessionId, ReentrantLock> locks = new ConcurrentHashMap<>();

    /** For host controls: only the owner may change it. "Not yours" looks the same as "not found". */
    public <R> R updateOwned(GameSessionId sessionId, OwnerId requesterId, Function<GameSession, R> change) {
        return update(sessionId, session -> {
            if (!session.getOwnerId().equals(requesterId)) {
                throw new GameSessionNotFoundException(sessionId);
            }
            return change.apply(session);
        });
    }

    public <R> R update(GameSessionId sessionId, Function<GameSession, R> change) {
        return run(sessionId, session -> {
            R result = change.apply(session);
            save(session);
            return result;
        });
    }

    /** Like update, but only saves (and pushes) when the change returns true. For buzzes: most are ignored. */
    public boolean updateIf(GameSessionId sessionId, Function<GameSession, Boolean> change) {
        return run(sessionId, session -> {
            boolean changed = change.apply(session);
            if (changed) {
                save(session);
            }
            return changed;
        });
    }

    private <R> R run(GameSessionId sessionId, Function<GameSession, R> work) {
        ReentrantLock lock = locks.computeIfAbsent(sessionId, id -> new ReentrantLock(true));
        lock.lock();
        try {
            GameSession session = loadGameSessionPort.loadGameSessionById(sessionId)
                    .orElseThrow(() -> new GameSessionNotFoundException(sessionId));
            return work.apply(session);
        } finally {
            lock.unlock();
        }
    }

    private void save(GameSession session) {
        saveGameSessionPort.saveGameSession(session);
        domainEventPublisher.publishAll(session.pullDomainEvents());
        gameSessionUpdatesPort.sessionChanged(session);
    }
}
