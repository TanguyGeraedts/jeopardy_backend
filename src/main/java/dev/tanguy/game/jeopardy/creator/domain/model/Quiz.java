package dev.tanguy.game.jeopardy.creator.domain.model;

import dev.tanguy.game.jeopardy.common.domain.model.id.CategoryId;
import dev.tanguy.game.jeopardy.common.domain.model.id.OwnerId;
import dev.tanguy.game.jeopardy.common.domain.model.id.QuestionId;
import dev.tanguy.game.jeopardy.common.domain.model.id.QuizId;
import dev.tanguy.game.jeopardy.creator.domain.event.quiz.CategoryNotFoundException;
import dev.tanguy.game.jeopardy.creator.domain.event.quiz.DuplicateCategoryNameException;
import dev.tanguy.game.jeopardy.creator.domain.event.quiz.InvalidQuizNameException;
import dev.tanguy.game.jeopardy.creator.domain.event.quiz.QuizNotPublishableException;
import dev.tanguy.game.jeopardy.creator.domain.event.quiz.TooManyTopicsException;
import lombok.Getter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Getter
public class Quiz {

    public static final int MAX_TOPICS = 3;
    public static final int MAX_AUTHOR_NAME_LENGTH = 60;

    private final QuizId id;
    private final OwnerId ownerId;
    private String name;
    private final List<Category> categories;

    /** Published = anyone signed in can find it in Discover and host it. New quizzes are private. */
    private boolean published;
    /** When it was (last) published: Discover's "Newest". Null while private. */
    private Instant publishedAt;
    /** The creator's display name when they published, shown as "by …". */
    private String authorName;
    private final Set<QuizTopic> topics = EnumSet.noneOf(QuizTopic.class);
    /** Games hosted with this quiz, by anyone. Only ever incremented in storage (never written back from here). */
    private int timesPlayed;
    /** Set by storage. Null on a quiz that was never saved. */
    private Instant createdAt;
    private Instant updatedAt;

    public Quiz(QuizId id, OwnerId ownerId, String name) {
        this(id, ownerId, name, new ArrayList<>());
    }

    public Quiz(QuizId id, OwnerId ownerId, String name, List<Category> categories) {
        this.id = Objects.requireNonNull(id, "QuizId cannot be null");
        this.ownerId = Objects.requireNonNull(ownerId, "OwnerId cannot be null");

        rename(name);

        this.categories = new ArrayList<>();
        if (categories != null) {
            this.categories.addAll(categories);
        }
    }

    public void rename(String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidQuizNameException(this.id);
        }
        this.name = name;
    }

    public void addCategory(Category category) {
        Objects.requireNonNull(category, "Category cannot be null");
        validateUniqueCategoryName(category.getName(), null);
        this.categories.add(category);
    }

    public Category getCategory(CategoryId categoryId) {
        Objects.requireNonNull(categoryId, "CategoryId cannot be null");
        return this.categories.stream()
                .filter(c -> c.getId().equals(categoryId))
                .findFirst()
                .orElseThrow(() -> new CategoryNotFoundException(this.id, categoryId));
    }

    public Category renameCategory(CategoryId categoryId, String newName) {
        Category category = getCategory(categoryId);
        if (newName != null) {
            validateUniqueCategoryName(newName.strip(), categoryId);
        }
        category.setName(newName == null ? null : newName.strip());
        return category;
    }

    public void removeCategory(CategoryId categoryId) {
        Objects.requireNonNull(categoryId, "CategoryId cannot be null");
        boolean removed = this.categories.removeIf(c -> c.getId().equals(categoryId));
        if (!removed) {
            throw new CategoryNotFoundException(this.id, categoryId);
        }
    }

    public List<Category> getCategories() {
        return Collections.unmodifiableList(categories);
    }

    public Set<QuizTopic> getTopics() {
        return Collections.unmodifiableSet(topics);
    }

    public int questionCount() {
        return categories.stream().mapToInt(c -> c.getQuestions().size()).sum();
    }

    /** Rebuilds the listing fields from storage. No validation. */
    public void restoreListing(
            boolean published, Instant publishedAt, String authorName, Collection<QuizTopic> topics,
            int timesPlayed, Instant createdAt, Instant updatedAt) {
        this.published = published;
        this.publishedAt = publishedAt;
        this.authorName = authorName;
        this.topics.clear();
        this.topics.addAll(topics);
        this.timesPlayed = timesPlayed;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /** Makes the quiz findable in Discover. Needs at least one question: an empty board can't be played. */
    public void publish(String authorName, Instant now) {
        if (questionCount() == 0) {
            throw new QuizNotPublishableException(id, "add at least one question first.");
        }
        String author = authorName == null ? "" : authorName.strip();
        if (author.isEmpty()) {
            throw new QuizNotPublishableException(id, "an author name is required.");
        }
        if (!published) {
            this.publishedAt = Objects.requireNonNull(now, "now cannot be null");
        }
        this.published = true;
        this.authorName = author.length() > MAX_AUTHOR_NAME_LENGTH ? author.substring(0, MAX_AUTHOR_NAME_LENGTH) : author;
    }

    /** Back to private. Games already running keep their copy of the board. */
    public void unpublish() {
        this.published = false;
        this.publishedAt = null;
    }

    public void changeTopics(Collection<QuizTopic> topics) {
        Set<QuizTopic> distinct = EnumSet.noneOf(QuizTopic.class);
        if (topics != null) {
            distinct.addAll(topics);
        }
        if (distinct.size() > MAX_TOPICS) {
            throw new TooManyTopicsException(id, MAX_TOPICS);
        }
        this.topics.clear();
        this.topics.addAll(distinct);
    }

    /**
     * A private copy for the same owner: same board and topics, new ids, never played.
     * Only for the owner: copying someone else's quiz would hand over their answers.
     */
    public Quiz duplicate(QuizId newId, String newName) {
        Quiz copy = new Quiz(newId, ownerId, newName);
        for (Category category : categories) {
            CategoryId categoryId = CategoryId.generate();
            List<Question> questions = category.getQuestions().stream()
                    .map(q -> new Question(QuestionId.generate(), categoryId, q.getPoints(), q.getQuestionText(),
                            q.getAnswerText(), q.getAnswerType(), q.getMediaUrl(), q.isDailyDouble()))
                    .toList();
            copy.categories.add(new Category(categoryId, newId, category.getName(), new ArrayList<>(questions)));
        }
        copy.topics.addAll(topics);
        return copy;
    }

    private void validateUniqueCategoryName(String name, CategoryId ignoreCategoryId) {
        boolean exists = this.categories.stream()
                .filter(c -> !c.getId().equals(ignoreCategoryId))
                .anyMatch(c -> c.getName().equalsIgnoreCase(name));
        if (exists) {
            throw new DuplicateCategoryNameException(this.id, name);
        }
    }
}