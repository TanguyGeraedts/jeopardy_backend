package dev.tanguy.game.jeopardy.creator.port.out;

import dev.tanguy.game.jeopardy.common.domain.model.id.OwnerId;
import dev.tanguy.game.jeopardy.common.domain.model.id.QuizId;
import dev.tanguy.game.jeopardy.creator.domain.model.Quiz;

import java.util.List;
import java.util.Optional;


public interface QuizRepositoryPort {

    Quiz save(Quiz quiz);

    Optional<Quiz> findById(QuizId quizId);

    List<Quiz> findByOwner(OwnerId ownerId);

    void deleteById(QuizId quizId);
}
