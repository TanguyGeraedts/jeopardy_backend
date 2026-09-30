package dev.tanguy.game.jeopardy.creator.core.category;

import dev.tanguy.game.jeopardy.creator.core.OwnedQuizFinder;
import dev.tanguy.game.jeopardy.creator.domain.model.Category;
import dev.tanguy.game.jeopardy.creator.domain.model.Quiz;
import dev.tanguy.game.jeopardy.creator.port.in.category.RenameCategoryCommand;
import dev.tanguy.game.jeopardy.creator.port.in.category.RenameCategoryUseCase;
import dev.tanguy.game.jeopardy.creator.port.out.QuizRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RenameCategoryUseCaseImpl implements RenameCategoryUseCase {

    private final OwnedQuizFinder ownedQuizFinder;
    private final QuizRepositoryPort quizRepositoryPort;

    @Override
    @Transactional
    public Category renameCategory(RenameCategoryCommand command) {
        Quiz quiz = ownedQuizFinder.find(command.quizId(), command.requesterId());

        quiz.renameCategory(command.categoryId(), command.name());

        Quiz saved = quizRepositoryPort.save(quiz);
        return saved.getCategory(command.categoryId());
    }
}