package dev.tanguy.game.jeopardy.creator.core.quiz;

import dev.tanguy.game.jeopardy.common.domain.model.id.QuizId;
import dev.tanguy.game.jeopardy.creator.domain.model.Quiz;
import dev.tanguy.game.jeopardy.creator.port.in.quiz.CreateQuizCommand;
import dev.tanguy.game.jeopardy.creator.port.in.quiz.CreateQuizUseCase;
import dev.tanguy.game.jeopardy.creator.port.out.QuizRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateQuizUseCaseImpl implements CreateQuizUseCase {

    private final QuizRepositoryPort quizRepositoryPort;

    @Override
    public Quiz createQuiz(CreateQuizCommand command) {
        Quiz quiz = new Quiz(QuizId.generate(), command.ownerId(), command.name());
        return quizRepositoryPort.save(quiz);
    }
}