package dev.tanguy.game.jeopardy.creator.port.in.quiz;

import dev.tanguy.game.jeopardy.common.domain.model.id.OwnerId;
import dev.tanguy.game.jeopardy.common.domain.model.id.QuizId;
import dev.tanguy.game.jeopardy.creator.domain.model.DiscoverSort;
import dev.tanguy.game.jeopardy.creator.domain.model.Quiz;
import dev.tanguy.game.jeopardy.creator.domain.model.QuizTopic;

import java.util.List;

/** Finding quizzes to host: everyone's published quizzes, plus your own. Callers show only the board's shape, never questions. */
public interface DiscoverQuizzesUseCase {

    int MAX_RESULTS = 60;

    /** text matches the name or author (blank = all); topic null = any. At most MAX_RESULTS. */
    List<Quiz> search(String text, QuizTopic topic, DiscoverSort sort);

    /** A quiz the requester may host: their own, or a published one. Otherwise QuizNotFoundException. */
    Quiz getHostable(QuizId quizId, OwnerId requesterId);
}
