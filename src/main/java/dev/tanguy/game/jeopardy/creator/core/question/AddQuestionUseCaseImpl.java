package dev.tanguy.game.jeopardy.creator.core.question;

import dev.tanguy.game.jeopardy.common.domain.model.id.QuestionId;
import dev.tanguy.game.jeopardy.creator.core.OwnedQuizFinder;
import dev.tanguy.game.jeopardy.creator.domain.model.Category;
import dev.tanguy.game.jeopardy.creator.domain.model.Question;
import dev.tanguy.game.jeopardy.creator.domain.model.Quiz;
import dev.tanguy.game.jeopardy.creator.port.in.question.AddQuestionCommand;
import dev.tanguy.game.jeopardy.creator.port.in.question.AddQuestionUseCase;
import dev.tanguy.game.jeopardy.creator.port.out.QuizRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AddQuestionUseCaseImpl implements AddQuestionUseCase {

    private final OwnedQuizFinder ownedQuizFinder;
    private final QuizRepositoryPort quizRepositoryPort;

    @Override
    @Transactional
    public Question addQuestion(AddQuestionCommand command) {
        Quiz quiz = ownedQuizFinder.find(command.quizId(), command.requesterId());
        Category category = quiz.getCategory(command.categoryId());

        QuestionId questionId = QuestionId.generate();
        category.addQuestion(new Question(
                questionId,
                category.getId(),
                command.points(),
                command.questionText(),
                command.answerText(),
                command.answerType(),
                command.mediaUrl(),
                command.dailyDouble()
        ));

        Quiz saved = quizRepositoryPort.save(quiz);
        return saved.getCategory(command.categoryId()).getQuestion(questionId);
    }
}