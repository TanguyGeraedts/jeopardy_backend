package dev.tanguy.game.jeopardy.creator.domain.model;

import dev.tanguy.game.jeopardy.common.domain.model.id.CategoryId;
import dev.tanguy.game.jeopardy.common.domain.model.id.OwnerId;
import dev.tanguy.game.jeopardy.common.domain.model.id.QuizId;
import dev.tanguy.game.jeopardy.creator.domain.event.quiz.CategoryNotFoundException;
import dev.tanguy.game.jeopardy.creator.domain.event.quiz.InvalidQuizNameException;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Getter
public class Quiz {

    private final QuizId id;
    private final OwnerId ownerId;
    private String name;
    private final List<Category> categories;

    public Quiz(QuizId id, OwnerId ownerId, String name) {
        this(id, ownerId, name, new ArrayList<>());
    }

    public Quiz(QuizId id, OwnerId ownerId, String name, List<Category> categories) {
        this.id = Objects.requireNonNull(id, "QuizId cannot be null");
        this.ownerId = Objects.requireNonNull(ownerId, "OwnerId cannot be null");

        setName(name);

        this.categories = new ArrayList<>();
        if (categories != null) {
            this.categories.addAll(categories);
        }
    }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidQuizNameException(this.id);
        }
        this.name = name;
    }

    public void addCategory(Category category) {
        Objects.requireNonNull(category, "Category cannot be null");
        this.categories.add(category);
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
}