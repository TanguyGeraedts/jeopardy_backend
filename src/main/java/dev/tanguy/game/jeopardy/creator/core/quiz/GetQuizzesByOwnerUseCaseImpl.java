package dev.tanguy.game.jeopardy.creator.core.quiz;

import dev.tanguy.game.jeopardy.creator.domain.model.Quiz;
import dev.tanguy.game.jeopardy.creator.port.in.quiz.GetQuizzesByOwnerQuery;
import dev.tanguy.game.jeopardy.creator.port.in.quiz.GetQuizzesByOwnerUseCase;
import dev.tanguy.game.jeopardy.creator.port.out.QuizRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetQuizzesByOwnerUseCaseImpl implements GetQuizzesByOwnerUseCase {

    private final QuizRepositoryPort quizRepositoryPort;

    @Override
    public List<Quiz> getMyQuizzes(GetQuizzesByOwnerQuery query) {
        return quizRepositoryPort.findByOwner(query.ownerId());
    }
}