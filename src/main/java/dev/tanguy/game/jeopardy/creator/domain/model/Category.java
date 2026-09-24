package dev.tanguy.game.jeopardy.creator.domain.model;

import dev.tanguy.game.jeopardy.common.domain.model.id.CategoryId;
import dev.tanguy.game.jeopardy.common.domain.model.id.QuestionId;
import dev.tanguy.game.jeopardy.common.domain.model.id.QuizId;
import dev.tanguy.game.jeopardy.creator.domain.event.category.DuplicateQuestionPointsException;
import dev.tanguy.game.jeopardy.creator.domain.event.category.InvalidCategoryNameException;
import dev.tanguy.game.jeopardy.creator.domain.event.category.QuestionNotFoundException;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Getter
public class Category {

    private final CategoryId id;
    private final QuizId quizId;
    private String name;
    private final List<Question> questions;

    public Category(CategoryId id, QuizId quizId, String name) {
        this(id, quizId, name, new ArrayList<>());
    }

    public Category(CategoryId id, QuizId quizId, String name, List<Question> questions) {
        this.id = Objects.requireNonNull(id, "CategoryId cannot be null");
        this.quizId = Objects.requireNonNull(quizId, "QuizId cannot be null");

        setName(name);

        this.questions = new ArrayList<>();
        if (questions != null) {
            for (Question q : questions) {
                addQuestion(q);
            }
        }
    }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidCategoryNameException(this.id);
        }
        this.name = name;
    }

    public void addQuestion(Question question) {
        Objects.requireNonNull(question, "Question cannot be null");
        validateNoDuplicatePoints(question.getPoints(), null);
        this.questions.add(question);
    }

    public void removeQuestion(QuestionId questionId) {
        Objects.requireNonNull(questionId, "QuestionId cannot be null");
        boolean removed = this.questions.removeIf(q -> q.getId().equals(questionId));
        if (!removed) {
            throw new QuestionNotFoundException(this.id, questionId);
        }
    }

    public List<Question> getQuestions() {
        return Collections.unmodifiableList(questions);
    }

    private void validateNoDuplicatePoints(int points, QuestionId ignoreQuestionId) {
        boolean pointsExist = this.questions.stream()
                .filter(q -> !q.getId().equals(ignoreQuestionId))
                .anyMatch(q -> q.getPoints() == points);

        if (pointsExist) {
            throw new DuplicateQuestionPointsException(this.id, points);
        }
    }
}