package dev.tanguy.game.jeopardy.creator.core;

import dev.tanguy.game.jeopardy.common.domain.model.id.OwnerId;
import dev.tanguy.game.jeopardy.common.domain.model.id.QuizId;
import dev.tanguy.game.jeopardy.creator.domain.event.quiz.QuizNotFoundException;
import dev.tanguy.game.jeopardy.creator.domain.model.Quiz;
import dev.tanguy.game.jeopardy.creator.port.out.QuizRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Loads a quiz only if the requester owns it. Missing and "not yours" are deliberately
 * indistinguishable to the caller (same rule as GetQuizUseCaseImpl).
 */
@Component
@RequiredArgsConstructor
public class OwnedQuizFinder {

    private final QuizRepositoryPort quizRepositoryPort;

    public Quiz find(QuizId quizId, OwnerId requesterId) {
        return quizRepositoryPort.findById(quizId)
                .filter(quiz -> quiz.getOwnerId().equals(requesterId))
                .orElseThrow(() -> new QuizNotFoundException(quizId));
    }
}