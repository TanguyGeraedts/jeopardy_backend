package dev.tanguy.game.jeopardy.creator.core.question;

import dev.tanguy.game.jeopardy.creator.core.OwnedQuizFinder;
import dev.tanguy.game.jeopardy.creator.domain.model.Question;
import dev.tanguy.game.jeopardy.creator.domain.model.Quiz;
import dev.tanguy.game.jeopardy.creator.port.in.question.UpdateQuestionCommand;
import dev.tanguy.game.jeopardy.creator.port.in.question.UpdateQuestionUseCase;
import dev.tanguy.game.jeopardy.creator.port.out.QuizRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateQuestionUseCaseImpl implements UpdateQuestionUseCase {

    private final OwnedQuizFinder ownedQuizFinder;
    private final QuizRepositoryPort quizRepositoryPort;

    @Override
    @Transactional
    public Question updateQuestion(UpdateQuestionCommand command) {
        Quiz quiz = ownedQuizFinder.find(command.quizId(), command.requesterId());

        quiz.getCategory(command.categoryId()).updateQuestion(
                command.questionId(),
                command.points(),
                command.questionText(),
                command.answerText(),
                command.answerType(),
                command.mediaUrl(),
                command.dailyDouble()
        );

        Quiz saved = quizRepositoryPort.save(quiz);
        return saved.getCategory(command.categoryId()).getQuestion(command.questionId());
    }
}