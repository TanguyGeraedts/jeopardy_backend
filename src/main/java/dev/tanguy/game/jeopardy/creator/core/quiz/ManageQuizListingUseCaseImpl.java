package dev.tanguy.game.jeopardy.creator.core.quiz;

import dev.tanguy.game.jeopardy.common.domain.model.id.OwnerId;
import dev.tanguy.game.jeopardy.common.domain.model.id.QuizId;
import dev.tanguy.game.jeopardy.creator.core.OwnedQuizFinder;
import dev.tanguy.game.jeopardy.creator.domain.model.Quiz;
import dev.tanguy.game.jeopardy.creator.domain.model.QuizTopic;
import dev.tanguy.game.jeopardy.creator.port.in.quiz.ManageQuizListingUseCase;
import dev.tanguy.game.jeopardy.creator.port.out.QuizRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Set;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class ManageQuizListingUseCaseImpl implements ManageQuizListingUseCase {

    private static final String COPY_SUFFIX = " (copy)";
    private static final int MAX_NAME_LENGTH = 255;

    private final OwnedQuizFinder ownedQuizFinder;
    private final QuizRepositoryPort quizRepositoryPort;

    @Override
    public Quiz setPublished(QuizId quizId, OwnerId requesterId, boolean published, String authorName) {
        return change(quizId, requesterId, quiz -> {
            if (published) quiz.publish(authorName, Instant.now());
            else quiz.unpublish();
        });
    }

    @Override
    public Quiz changeTopics(QuizId quizId, OwnerId requesterId, Set<QuizTopic> topics) {
        return change(quizId, requesterId, quiz -> quiz.changeTopics(topics));
    }

    @Override
    public Quiz duplicate(QuizId quizId, OwnerId requesterId) {
        Quiz original = ownedQuizFinder.find(quizId, requesterId);
        String name = original.getName().length() + COPY_SUFFIX.length() > MAX_NAME_LENGTH
                ? original.getName().substring(0, MAX_NAME_LENGTH - COPY_SUFFIX.length()) + COPY_SUFFIX
                : original.getName() + COPY_SUFFIX;
        return quizRepositoryPort.save(original.duplicate(QuizId.generate(), name));
    }

    private Quiz change(QuizId quizId, OwnerId requesterId, Consumer<Quiz> change) {
        Quiz quiz = ownedQuizFinder.find(quizId, requesterId);
        change.accept(quiz);
        return quizRepositoryPort.save(quiz);
    }
}
