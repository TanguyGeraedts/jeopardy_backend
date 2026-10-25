package dev.tanguy.game.jeopardy.creator.port.in.quiz;

import dev.tanguy.game.jeopardy.common.domain.model.id.OwnerId;
import dev.tanguy.game.jeopardy.common.domain.model.id.QuizId;
import dev.tanguy.game.jeopardy.creator.domain.model.Quiz;
import dev.tanguy.game.jeopardy.creator.domain.model.QuizTopic;

import java.util.Set;

/**
 * How the owner presents a quiz: public or private, its topics, and copies. Grouped because each is a one-liner on Quiz.
 * Every method throws QuizNotFoundException if the requester doesn't own the quiz, and returns the quiz as saved.
 */
public interface ManageQuizListingUseCase {

    /** published = true needs authorName (shown as "by …" in Discover). */
    Quiz setPublished(QuizId quizId, OwnerId requesterId, boolean published, String authorName);

    Quiz changeTopics(QuizId quizId, OwnerId requesterId, Set<QuizTopic> topics);

    /** A private copy named "… (copy)". Owner only: copying someone else's quiz would hand over their answers. */
    Quiz duplicate(QuizId quizId, OwnerId requesterId);
}
