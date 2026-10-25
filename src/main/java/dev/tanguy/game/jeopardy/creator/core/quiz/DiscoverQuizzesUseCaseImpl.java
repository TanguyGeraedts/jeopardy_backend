package dev.tanguy.game.jeopardy.creator.core.quiz;

import dev.tanguy.game.jeopardy.common.domain.model.id.OwnerId;
import dev.tanguy.game.jeopardy.common.domain.model.id.QuizId;
import dev.tanguy.game.jeopardy.creator.core.HostableQuizFinder;
import dev.tanguy.game.jeopardy.creator.domain.model.DiscoverSort;
import dev.tanguy.game.jeopardy.creator.domain.model.Quiz;
import dev.tanguy.game.jeopardy.creator.domain.model.QuizTopic;
import dev.tanguy.game.jeopardy.creator.port.in.quiz.DiscoverQuizzesUseCase;
import dev.tanguy.game.jeopardy.creator.port.out.QuizRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DiscoverQuizzesUseCaseImpl implements DiscoverQuizzesUseCase {

    private final QuizRepositoryPort quizRepositoryPort;
    private final HostableQuizFinder hostableQuizFinder;

    @Override
    public List<Quiz> search(String text, QuizTopic topic, DiscoverSort sort) {
        return quizRepositoryPort.searchPublished(text, topic, sort == null ? DiscoverSort.POPULAR : sort, MAX_RESULTS);
    }

    @Override
    public Quiz getHostable(QuizId quizId, OwnerId requesterId) {
        return hostableQuizFinder.find(quizId, requesterId);
    }
}
