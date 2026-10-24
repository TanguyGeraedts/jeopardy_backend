package dev.tanguy.game.jeopardy.gameplay.core.session;

import dev.tanguy.game.jeopardy.common.domain.model.id.ClueId;
import dev.tanguy.game.jeopardy.gameplay.domain.exception.clue.ClueNotFoundException;
import dev.tanguy.game.jeopardy.gameplay.domain.model.ClueState;
import dev.tanguy.game.jeopardy.gameplay.port.in.session.AnswerClueCommand;
import dev.tanguy.game.jeopardy.gameplay.port.in.session.AnswerClueResult;
import dev.tanguy.game.jeopardy.gameplay.port.in.session.AnswerClueUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AnswerClueUseCaseImpl implements AnswerClueUseCase {

    private final GameSessionUpdater updater;

    @Override
    public AnswerClueResult answerClue(AnswerClueCommand command) {
        return updater.updateOwned(command.sessionId(), command.requesterId(), session -> {
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

            return new AnswerClueResult(isCorrect, scoreDelta, clue.getAnswer());
        });
    }

    private boolean matches(String submitted, String expected) {
        if (submitted == null || expected == null) {
            return false;
        }
        return submitted.trim().equalsIgnoreCase(expected.trim());
    }
}
