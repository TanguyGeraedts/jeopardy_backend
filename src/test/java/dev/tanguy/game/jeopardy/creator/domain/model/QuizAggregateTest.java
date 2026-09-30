package dev.tanguy.game.jeopardy.creator.domain.model;

import dev.tanguy.game.jeopardy.common.domain.model.id.CategoryId;
import dev.tanguy.game.jeopardy.common.domain.model.id.OwnerId;
import dev.tanguy.game.jeopardy.common.domain.model.id.QuestionId;
import dev.tanguy.game.jeopardy.common.domain.model.id.QuizId;
import dev.tanguy.game.jeopardy.creator.domain.event.category.DuplicateQuestionPointsException;
import dev.tanguy.game.jeopardy.creator.domain.event.category.QuestionNotFoundException;
import dev.tanguy.game.jeopardy.creator.domain.event.question.MissingMediaUrlException;
import dev.tanguy.game.jeopardy.creator.domain.event.quiz.CategoryNotFoundException;
import dev.tanguy.game.jeopardy.creator.domain.event.quiz.DuplicateCategoryNameException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QuizAggregateTest {

    private Quiz quiz;
    private Category history;

    @BeforeEach
    void setUp() {
        quiz = new Quiz(QuizId.generate(), OwnerId.generate(), "Trivia");
        history = new Category(CategoryId.generate(), quiz.getId(), "History");
        quiz.addCategory(history);
    }

    private Question question(int points) {
        return new Question(QuestionId.generate(), history.getId(), points, "Q?", "A", AnswerType.TEXT, null);
    }

    // ---------- categories ----------

    @Test
    void addCategory_rejectsDuplicateNameIgnoringCase() {
        Category duplicate = new Category(CategoryId.generate(), quiz.getId(), "history");

        assertThatThrownBy(() -> quiz.addCategory(duplicate))
                .isInstanceOf(DuplicateCategoryNameException.class);
    }

    @Test
    void renameCategory_toItsOwnName_isAllowed() {
        quiz.renameCategory(history.getId(), "HISTORY");

        assertThat(quiz.getCategory(history.getId()).getName()).isEqualTo("HISTORY");
    }

    @Test
    void renameCategory_toAnotherCategorysName_isRejected() {
        quiz.addCategory(new Category(CategoryId.generate(), quiz.getId(), "Science"));

        assertThatThrownBy(() -> quiz.renameCategory(history.getId(), " science "))
                .isInstanceOf(DuplicateCategoryNameException.class);
    }

    @Test
    void getCategory_unknownId_throwsNotFound() {
        assertThatThrownBy(() -> quiz.getCategory(CategoryId.generate()))
                .isInstanceOf(CategoryNotFoundException.class);
    }

    // ---------- questions ----------

    @Test
    void addQuestion_rejectsDuplicatePoints() {
        history.addQuestion(question(200));

        assertThatThrownBy(() -> history.addQuestion(question(200)))
                .isInstanceOf(DuplicateQuestionPointsException.class);
    }

    @Test
    void updateQuestion_canKeepItsOwnPoints() {
        Question q = question(200);
        history.addQuestion(q);

        history.updateQuestion(q.getId(), 200, "New?", "New", AnswerType.TEXT, null, true);

        assertThat(history.getQuestion(q.getId()).getQuestionText()).isEqualTo("New?");
        assertThat(history.getQuestion(q.getId()).isDailyDouble()).isTrue();
    }

    @Test
    void updateQuestion_cannotTakePointsOfAnotherQuestion() {
        history.addQuestion(question(200));
        Question other = question(400);
        history.addQuestion(other);

        assertThatThrownBy(() ->
                history.updateQuestion(other.getId(), 200, "Q?", "A", AnswerType.TEXT, null, false))
                .isInstanceOf(DuplicateQuestionPointsException.class);
        assertThat(history.getQuestion(other.getId()).getPoints()).isEqualTo(400);
    }

    @Test
    void updateQuestion_imageWithoutUrl_isRejected() {
        Question q = question(200);
        history.addQuestion(q);

        assertThatThrownBy(() ->
                history.updateQuestion(q.getId(), 200, "Q?", "A", AnswerType.IMAGE, " ", false))
                .isInstanceOf(MissingMediaUrlException.class);
    }

    @Test
    void textAnswer_dropsStrayMediaUrl() {
        Question q = new Question(QuestionId.generate(), history.getId(), 100, "Q?", "A",
                AnswerType.TEXT, "https://example.com/x.png");

        assertThat(q.getMediaUrl()).isNull();
    }

    @Test
    void getQuestion_unknownId_throwsNotFound() {
        assertThatThrownBy(() -> history.getQuestion(QuestionId.generate()))
                .isInstanceOf(QuestionNotFoundException.class);
    }

    @Test
    void removeQuestion_thenPointsBecomeFreeAgain() {
        Question q = question(200);
        history.addQuestion(q);

        history.removeQuestion(q.getId());
        history.addQuestion(question(200));

        assertThat(history.getQuestions()).hasSize(1);
    }
}