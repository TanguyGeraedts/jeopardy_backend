package dev.tanguy.game.jeopardy.creator.core.quiz;

import dev.tanguy.game.jeopardy.creator.domain.event.quiz.QuizNotFoundException;
import dev.tanguy.game.jeopardy.creator.port.in.quiz.DeleteQuizCommand;
import dev.tanguy.game.jeopardy.creator.port.in.quiz.DeleteQuizUseCase;
import dev.tanguy.game.jeopardy.creator.port.out.QuizRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteQuizUseCaseImpl implements DeleteQuizUseCase {

    private final QuizRepositoryPort quizRepositoryPort;

    @Override
    @Transactional
    public void deleteQuiz(DeleteQuizCommand command) {
        quizRepositoryPort.findById(command.quizId())
                .filter(q -> q.getOwnerId().equals(command.requesterId()))
                .orElseThrow(() -> new QuizNotFoundException(command.quizId()));

        quizRepositoryPort.deleteById(command.quizId());
    }
}