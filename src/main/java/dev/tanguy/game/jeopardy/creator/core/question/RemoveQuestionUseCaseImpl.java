package dev.tanguy.game.jeopardy.creator.core.question;

import dev.tanguy.game.jeopardy.creator.core.OwnedQuizFinder;
import dev.tanguy.game.jeopardy.creator.domain.model.Quiz;
import dev.tanguy.game.jeopardy.creator.port.in.question.RemoveQuestionCommand;
import dev.tanguy.game.jeopardy.creator.port.in.question.RemoveQuestionUseCase;
import dev.tanguy.game.jeopardy.creator.port.out.QuizRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RemoveQuestionUseCaseImpl implements RemoveQuestionUseCase {

    private final OwnedQuizFinder ownedQuizFinder;
    private final QuizRepositoryPort quizRepositoryPort;

    @Override
    @Transactional
    public void removeQuestion(RemoveQuestionCommand command) {
        Quiz quiz = ownedQuizFinder.find(command.quizId(), command.requesterId());

        quiz.getCategory(command.categoryId()).removeQuestion(command.questionId());

        quizRepositoryPort.save(quiz);
    }
}