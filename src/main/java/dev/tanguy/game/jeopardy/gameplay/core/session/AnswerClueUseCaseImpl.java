package dev.tanguy.game.jeopardy.gameplay.core.session;

import dev.tanguy.game.jeopardy.common.domain.model.id.ClueId;
import dev.tanguy.game.jeopardy.common.events.DomainEventPublisher;
import dev.tanguy.game.jeopardy.gameplay.domain.exception.clue.ClueNotFoundException;
import dev.tanguy.game.jeopardy.gameplay.domain.exception.session.GameSessionNotFoundException;
import dev.tanguy.game.jeopardy.gameplay.domain.model.ClueState;
import dev.tanguy.game.jeopardy.gameplay.domain.model.GameSession;
import dev.tanguy.game.jeopardy.gameplay.port.in.session.AnswerClueCommand;
import dev.tanguy.game.jeopardy.gameplay.port.in.session.AnswerClueResult;
import dev.tanguy.game.jeopardy.gameplay.port.in.session.AnswerClueUseCase;
import dev.tanguy.game.jeopardy.gameplay.port.out.session.LoadGameSessionPort;
import dev.tanguy.game.jeopardy.gameplay.port.out.session.SaveGameSessionPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AnswerClueUseCaseImpl implements AnswerClueUseCase {

    private final LoadGameSessionPort loadGameSessionPort;
    private final SaveGameSessionPort saveGameSessionPort;
    private final DomainEventPublisher domainEventPublisher;

    @Override
    public AnswerClueResult answerClue(AnswerClueCommand command) {
        GameSession session = loadGameSessionPort.loadGameSessionById(command.sessionId())
                .orElseThrow(() -> new GameSessionNotFoundException(command.sessionId()));

        ClueId activeClueId = session.getActiveClueId();
        if (activeClueId == null || !activeClueId.equals(command.clueId())) {
            throw new ClueNotFoundException(command.clueId());
        }

        ClueState clue = session.getClues().get(activeClueId);
        if (clue == null) {
            throw new ClueNotFoundException(activeClueId);
        }

        boolean isCorrect = matches(command.answer(), clue.getAnswer());
        int scoreDelta = isCorrect ? clue.getValue() : -clue.getValue();

        session.evaluateAnswer(isCorrect);

        saveGameSessionPort.saveGameSession(session);
        domainEventPublisher.publishAll(session.pullDomainEvents());

        return new AnswerClueResult(isCorrect, scoreDelta, clue.getAnswer());
    }

    private boolean matches(String submitted, String expected) {
        if (submitted == null || expected == null) {
            return false;
        }
        return submitted.trim().equalsIgnoreCase(expected.trim());
    }
}