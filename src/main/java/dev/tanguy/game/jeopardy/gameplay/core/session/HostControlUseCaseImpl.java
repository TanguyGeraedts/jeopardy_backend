package dev.tanguy.game.jeopardy.gameplay.core.session;

import dev.tanguy.game.jeopardy.common.domain.model.id.ClueId;
import dev.tanguy.game.jeopardy.common.domain.model.id.GameSessionId;
import dev.tanguy.game.jeopardy.common.domain.model.id.OwnerId;
import dev.tanguy.game.jeopardy.common.domain.model.id.TeamId;
import dev.tanguy.game.jeopardy.gameplay.domain.model.BuzzerSound;
import dev.tanguy.game.jeopardy.gameplay.domain.model.GameSession;
import dev.tanguy.game.jeopardy.gameplay.port.in.session.HostControlUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class HostControlUseCaseImpl implements HostControlUseCase {

    private final GameSessionUpdater updater;

    @Override
    public GameSession selectClue(GameSessionId sessionId, OwnerId requesterId, ClueId clueId) {
        return change(sessionId, requesterId, session -> session.selectClue(clueId));
    }

    @Override
    public GameSession openBuzzers(GameSessionId sessionId, OwnerId requesterId) {
        return change(sessionId, requesterId, GameSession::openBuzzers);
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
