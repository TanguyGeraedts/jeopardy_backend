package dev.tanguy.game.jeopardy.creator.domain.model;

import dev.tanguy.game.jeopardy.common.domain.model.id.CategoryId;
import dev.tanguy.game.jeopardy.common.domain.model.id.OwnerId;
import dev.tanguy.game.jeopardy.common.domain.model.id.QuestionId;
import dev.tanguy.game.jeopardy.common.domain.model.id.QuizId;
import dev.tanguy.game.jeopardy.creator.domain.event.category.DuplicateQuestionPointsException;
import dev.tanguy.game.jeopardy.creator.domain.event.category.QuestionNotFoundException;
import dev.tanguy.game.jeopardy.creator.domain.event.question.MissingMediaUrlException;
import dev.tanguy.game.jeopardy.creator.domain.event.question.UnsupportedAudioUrlException;
import dev.tanguy.game.jeopardy.creator.domain.event.quiz.CategoryNotFoundException;
import dev.tanguy.game.jeopardy.creator.domain.event.quiz.DuplicateCategoryNameException;
import dev.tanguy.game.jeopardy.creator.domain.event.quiz.QuizNotPublishableException;
import dev.tanguy.game.jeopardy.creator.domain.event.quiz.TooManyTopicsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

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
    void audio_needsAUrl_andCannotBeYoutube() {
        Question q = question(200);
        history.addQuestion(q);

        assertThatThrownBy(() -> history.updateQuestion(q.getId(), 200, "Q?", "A", AnswerType.AUDIO, " ", false))
                .isInstanceOf(MissingMediaUrlException.class);
        assertThatThrownBy(() -> history.updateQuestion(q.getId(), 200, "Q?", "A", AnswerType.AUDIO, "https://youtu.be/dQw4w9WgXcQ", false))
                .isInstanceOf(UnsupportedAudioUrlException.class);
        assertThatThrownBy(() -> history.updateQuestion(q.getId(), 200, "Q?", "A", AnswerType.AUDIO, "https://www.youtube.com/watch?v=dQw4w9WgXcQ", false))
                .isInstanceOf(UnsupportedAudioUrlException.class);
        // Hosts are case-insensitive, and unencoded characters must not make the check give up.
        for (String sneaky : new String[] {
                "https://WWW.YouTube.com/watch?v=dQw4w9WgXcQ",
                "https://www.youtube.com/watch?v=dQw4w9WgXcQ&list=a b",
                "https://youtu.be./dQw4w9WgXcQ"}) {
            assertThatThrownBy(() -> history.updateQuestion(q.getId(), 200, "Q?", "A", AnswerType.AUDIO, sneaky, false))
                    .as(sneaky)
                    .isInstanceOf(UnsupportedAudioUrlException.class);
        }
        // Look-alikes are not YouTube.
        history.updateQuestion(q.getId(), 200, "Q?", "A", AnswerType.AUDIO, "https://notyoutube.com/clip.mp3", false);

        history.updateQuestion(q.getId(), 200, "Q?", "A", AnswerType.AUDIO, "https://example.com/intro.mp3", false);
        assertThat(history.getQuestion(q.getId()).getAnswerType()).isEqualTo(AnswerType.AUDIO);
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

    // ---------- listing (Discover) ----------

    private static final Instant NOW = Instant.parse("2026-10-25T20:00:00Z");

    @Test
    void publish_needsAtLeastOneQuestion() {
        assertThatThrownBy(() -> quiz.publish("Tanguy", NOW)).isInstanceOf(QuizNotPublishableException.class);
        assertThat(quiz.isPublished()).isFalse();
    }

    @Test
    void publish_needsAnAuthorName() {
        history.addQuestion(question(100));

        assertThatThrownBy(() -> quiz.publish("  ", NOW)).isInstanceOf(QuizNotPublishableException.class);
    }

    @Test
    void publish_thenUnpublish() {
        history.addQuestion(question(100));

        quiz.publish("  Tanguy ", NOW);
        quiz.publish("Tanguy G", NOW.plusSeconds(60)); // re-publishing keeps the original date

        assertThat(quiz.isPublished()).isTrue();
        assertThat(quiz.getAuthorName()).isEqualTo("Tanguy G");
        assertThat(quiz.getPublishedAt()).isEqualTo(NOW);

        quiz.unpublish();
        assertThat(quiz.isPublished()).isFalse();
        assertThat(quiz.getPublishedAt()).isNull();
    }

    @Test
    void topics_atMostThree() {
        quiz.changeTopics(List.of(QuizTopic.MUSIC, QuizTopic.HISTORY, QuizTopic.MUSIC));
        assertThat(quiz.getTopics()).containsExactlyInAnyOrder(QuizTopic.MUSIC, QuizTopic.HISTORY);

        assertThatThrownBy(() -> quiz.changeTopics(List.of(QuizTopic.MUSIC, QuizTopic.HISTORY, QuizTopic.SPORTS, QuizTopic.GAMES)))
                .isInstanceOf(TooManyTopicsException.class);
        assertThat(quiz.getTopics()).hasSize(2);
    }

    @Test
    void duplicate_isAPrivateCopyWithNewIds() {
        history.addQuestion(question(100));
        quiz.changeTopics(List.of(QuizTopic.HISTORY));
        quiz.publish("Tanguy", NOW);

        Quiz copy = quiz.duplicate(QuizId.generate(), "Trivia (copy)");

        assertThat(copy.getOwnerId()).isEqualTo(quiz.getOwnerId());
        assertThat(copy.isPublished()).isFalse();
        assertThat(copy.getTopics()).containsExactly(QuizTopic.HISTORY);
        assertThat(copy.getCategories()).singleElement().satisfies(category -> {
            assertThat(category.getName()).isEqualTo("History");
            assertThat(category.getId()).isNotEqualTo(history.getId());
            assertThat(category.getQuestions()).singleElement().satisfies(q -> {
                assertThat(q.getPoints()).isEqualTo(100);
                assertThat(q.getId()).isNotEqualTo(history.getQuestions().getFirst().getId());
                assertThat(q.getCategoryId()).isEqualTo(category.getId());
            });
        });
    }
}