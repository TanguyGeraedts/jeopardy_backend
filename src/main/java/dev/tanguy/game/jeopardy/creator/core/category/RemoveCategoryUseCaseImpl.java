package dev.tanguy.game.jeopardy.creator.core.category;

import dev.tanguy.game.jeopardy.creator.core.OwnedQuizFinder;
import dev.tanguy.game.jeopardy.creator.domain.model.Quiz;
import dev.tanguy.game.jeopardy.creator.port.in.category.RemoveCategoryCommand;
import dev.tanguy.game.jeopardy.creator.port.in.category.RemoveCategoryUseCase;
import dev.tanguy.game.jeopardy.creator.port.out.QuizRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RemoveCategoryUseCaseImpl implements RemoveCategoryUseCase {

    private final OwnedQuizFinder ownedQuizFinder;
    private final QuizRepositoryPort quizRepositoryPort;

    @Override
    @Transactional
    public void removeCategory(RemoveCategoryCommand command) {
        Quiz quiz = ownedQuizFinder.find(command.quizId(), command.requesterId());

        quiz.removeCategory(command.categoryId());

        quizRepositoryPort.save(quiz);
    }
}