package dev.tanguy.game.jeopardy.creator.core.quiz;

import dev.tanguy.game.jeopardy.common.domain.model.id.OwnerId;
import dev.tanguy.game.jeopardy.common.domain.model.id.QuizId;
import dev.tanguy.game.jeopardy.creator.api.QuizSnapshot;
import dev.tanguy.game.jeopardy.creator.api.QuizSnapshot.CategorySnapshot;
import dev.tanguy.game.jeopardy.creator.api.QuizSnapshot.QuestionSnapshot;
import dev.tanguy.game.jeopardy.creator.api.QuizSnapshotQuery;
import dev.tanguy.game.jeopardy.creator.core.HostableQuizFinder;
import dev.tanguy.game.jeopardy.creator.domain.model.AnswerType;
import dev.tanguy.game.jeopardy.creator.domain.model.Quiz;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QuizSnapshotQueryImpl implements QuizSnapshotQuery {

    private final HostableQuizFinder hostableQuizFinder;

    @Override
    public QuizSnapshot getSnapshot(QuizId quizId, OwnerId requesterId) {
        Quiz quiz = hostableQuizFinder.find(quizId, requesterId);

        return new QuizSnapshot(
                quiz.getId(),
                quiz.getName(),
                quiz.getCategories().stream()
                        .map(category -> new CategorySnapshot(
                                category.getId(),
                                category.getName(),
                                category.getQuestions().stream()
                                        .map(q -> new QuestionSnapshot(
                                                q.getId(),
                                                q.getPoints(),
                                                q.getQuestionText(),
                                                q.getAnswerText(),
                                                q.isDailyDouble(),
                                                q.getAnswerType() == AnswerType.TEXT ? null : q.getAnswerType().name(),
                                                q.getMediaUrl()))
                                        .toList()))
                        .toList());
    }
}