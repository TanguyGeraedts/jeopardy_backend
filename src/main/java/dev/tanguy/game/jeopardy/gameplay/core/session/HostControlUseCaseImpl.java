package dev.tanguy.game.jeopardy.gameplay.core.session;

import dev.tanguy.game.jeopardy.common.domain.model.id.ClueId;
import dev.tanguy.game.jeopardy.common.domain.model.id.GameSessionId;
import dev.tanguy.game.jeopardy.common.domain.model.id.OwnerId;
import dev.tanguy.game.jeopardy.common.domain.model.id.TeamId;
import dev.tanguy.game.jeopardy.gameplay.domain.model.BuzzerSound;
import dev.tanguy.game.jeopardy.gameplay.domain.model.GameSession;
import dev.tanguy.game.jeopardy.gameplay.domain.model.TurnRules;
import dev.tanguy.game.jeopardy.gameplay.port.in.session.HostControlUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class HostControlUseCaseImpl implements HostControlUseCase {

    static final long MIN_OPEN_DELAY_MS = 500;
    static final long MAX_OPEN_DELAY_MS = 2500;

    private final GameSessionUpdater updater;

    @Override
    public GameSession selectClue(GameSessionId sessionId, OwnerId requesterId, ClueId clueId) {
        return change(sessionId, requesterId, session -> session.selectClue(clueId));
    }

    @Override
    public GameSession openBuzzers(GameSessionId sessionId, OwnerId requesterId) {
        // Same clock as the buzzes: taken before the lock, like theirs.
        Instant openedAt = Instant.now();
        // Used only without turns: the buzzers open somewhere in this window after the click, so nobody can time it.
        Duration randomDelay = Duration.ofMillis(ThreadLocalRandom.current().nextLong(MIN_OPEN_DELAY_MS, MAX_OPEN_DELAY_MS + 1));
        return change(sessionId, requesterId, session -> session.openBuzzers(openedAt, randomDelay));
    }

    @Override
    public GameSession changeTurnRules(GameSessionId sessionId, OwnerId requesterId, TurnRules rules) {
        return change(sessionId, requesterId, session -> session.changeTurnRules(rules));
    }

    @Override
    public GameSession giveTurn(GameSessionId sessionId, OwnerId requesterId, TeamId teamId) {
        return change(sessionId, requesterId, session -> session.giveTurn(teamId));
    }

    @Override
    public GameSession skipTurn(GameSessionId sessionId, OwnerId requesterId) {
        return change(sessionId, requesterId, GameSession::skipTurn);
    }

    @Override
    public GameSession judgeAnswer(GameSessionId sessionId, OwnerId requesterId, boolean correct) {
        return change(sessionId, requesterId, session -> session.evaluateAnswer(correct));
    }

    @Override
    public GameSession closeClue(GameSessionId sessionId, OwnerId requesterId) {
        return change(sessionId, requesterId, GameSession::closeClue);
    }

    @Override
    public GameSession endGame(GameSessionId sessionId, OwnerId requesterId) {
        return change(sessionId, requesterId, GameSession::endGame);
    }

    @Override
    public GameSession changeScore(GameSessionId sessionId, OwnerId requesterId, TeamId teamId, Integer delta, Integer score) {
        if ((delta == null) == (score == null)) {
            throw new IllegalArgumentException("Send either delta or score.");
        }
        return change(sessionId, requesterId, session -> {
            if (delta != null) session.adjustScore(teamId, delta);
            else session.setScore(teamId, score);
        });
    }

    @Override
    public GameSession setTeamBuzzerSound(GameSessionId sessionId, OwnerId requesterId, TeamId teamId, BuzzerSound sound) {
        return change(sessionId, requesterId, session -> session.setTeamBuzzerSound(teamId, sound));
    }

    private GameSession change(GameSessionId sessionId, OwnerId requesterId, Consumer<GameSession> change) {
        return updater.updateOwned(sessionId, requesterId, session -> {
            change.accept(session);
            return session;
        });
    }
}
