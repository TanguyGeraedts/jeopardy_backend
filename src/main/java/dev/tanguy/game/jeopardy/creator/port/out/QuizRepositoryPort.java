package dev.tanguy.game.jeopardy.creator.port.out;

import dev.tanguy.game.jeopardy.common.domain.model.id.OwnerId;
import dev.tanguy.game.jeopardy.common.domain.model.id.QuizId;
import dev.tanguy.game.jeopardy.creator.domain.model.Quiz;
import dev.tanguy.game.jeopardy.creator.domain.model.DiscoverSort;
import dev.tanguy.game.jeopardy.creator.domain.model.QuizTopic;

import java.util.List;
import java.util.Optional;


public interface QuizRepositoryPort {

    Quiz save(Quiz quiz);

    Optional<Quiz> findById(QuizId quizId);

    List<Quiz> findByOwner(OwnerId ownerId);

    void deleteById(QuizId quizId);

    /** Published quizzes. text matches name or author (case-insensitive, blank = all); topic null = any. */
    List<Quiz> searchPublished(String text, QuizTopic topic, DiscoverSort sort, int limit);

    /** +1 game hosted. Atomic, so two hosts starting at once both count. */
    void incrementTimesPlayed(QuizId quizId);
}
