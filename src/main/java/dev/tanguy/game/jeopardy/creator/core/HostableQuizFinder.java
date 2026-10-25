package dev.tanguy.game.jeopardy.creator.core;

import dev.tanguy.game.jeopardy.common.domain.model.id.OwnerId;
import dev.tanguy.game.jeopardy.common.domain.model.id.QuizId;
import dev.tanguy.game.jeopardy.creator.domain.event.quiz.QuizNotFoundException;
import dev.tanguy.game.jeopardy.creator.domain.model.Quiz;
import dev.tanguy.game.jeopardy.creator.port.out.QuizRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Loads a quiz the requester may host: their own, or anyone's published one. Like OwnedQuizFinder, a private quiz of
 * someone else looks exactly like a missing one. Editing still goes through OwnedQuizFinder.
 */
@Component
@RequiredArgsConstructor
public class HostableQuizFinder {

    private final QuizRepositoryPort quizRepositoryPort;

    public Quiz find(QuizId quizId, OwnerId requesterId) {
        return quizRepositoryPort.findById(quizId)
                .filter(quiz -> quiz.isPublished() || quiz.getOwnerId().equals(requesterId))
                .orElseThrow(() -> new QuizNotFoundException(quizId));
    }
}
