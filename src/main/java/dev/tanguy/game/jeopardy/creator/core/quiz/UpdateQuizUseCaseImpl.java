package dev.tanguy.game.jeopardy.creator.core.quiz;

import dev.tanguy.game.jeopardy.creator.domain.event.quiz.QuizNotFoundException;
import dev.tanguy.game.jeopardy.creator.domain.model.Quiz;
import dev.tanguy.game.jeopardy.creator.port.in.quiz.UpdateQuizCommand;
import dev.tanguy.game.jeopardy.creator.port.in.quiz.UpdateQuizUseCase;
import dev.tanguy.game.jeopardy.creator.port.out.QuizRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateQuizUseCaseImpl implements UpdateQuizUseCase {

    private final QuizRepositoryPort quizRepositoryPort;

    @Override
    @Transactional
    public Quiz updateQuiz(UpdateQuizCommand command) {
        Quiz quiz = quizRepositoryPort.findById(command.quizId())
                .filter(q -> q.getOwnerId().equals(command.requesterId()))
                .orElseThrow(() -> new QuizNotFoundException(command.quizId()));

        quiz.rename(command.name());

        return quizRepositoryPort.save(quiz);
    }
}