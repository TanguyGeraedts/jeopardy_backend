package dev.tanguy.game.jeopardy.creator.core.category;

import dev.tanguy.game.jeopardy.common.domain.model.id.CategoryId;
import dev.tanguy.game.jeopardy.creator.core.OwnedQuizFinder;
import dev.tanguy.game.jeopardy.creator.domain.model.Category;
import dev.tanguy.game.jeopardy.creator.domain.model.Quiz;
import dev.tanguy.game.jeopardy.creator.port.in.category.AddCategoryCommand;
import dev.tanguy.game.jeopardy.creator.port.in.category.AddCategoryUseCase;
import dev.tanguy.game.jeopardy.creator.port.out.QuizRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AddCategoryUseCaseImpl implements AddCategoryUseCase {

    private final OwnedQuizFinder ownedQuizFinder;
    private final QuizRepositoryPort quizRepositoryPort;

    @Override
    @Transactional
    public Category addCategory(AddCategoryCommand command) {
        Quiz quiz = ownedQuizFinder.find(command.quizId(), command.requesterId());

        CategoryId categoryId = CategoryId.generate();
        quiz.addCategory(new Category(categoryId, quiz.getId(), command.name()));

        Quiz saved = quizRepositoryPort.save(quiz);
        return saved.getCategory(categoryId);
    }
}