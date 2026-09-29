package dev.tanguy.game.jeopardy.creator.core.quiz;

import dev.tanguy.game.jeopardy.creator.domain.event.quiz.QuizNotFoundException;
import dev.tanguy.game.jeopardy.creator.domain.model.Quiz;
import dev.tanguy.game.jeopardy.creator.port.in.quiz.GetQuizQuery;
import dev.tanguy.game.jeopardy.creator.port.in.quiz.GetQuizUseCase;
import dev.tanguy.game.jeopardy.creator.port.out.QuizRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetQuizUseCaseImpl implements GetQuizUseCase {

    private final QuizRepositoryPort quizRepositoryPort;

    @Override
    public Quiz getQuiz(GetQuizQuery query) {
        // Missing and "not yours" are deliberately indistinguishable to the caller.
        return quizRepositoryPort.findById(query.quizId())
                .filter(quiz -> quiz.getOwnerId().equals(query.requesterId()))
                .orElseThrow(() -> new QuizNotFoundException(query.quizId()));
    }
}