package dev.tanguy.game.jeopardy.creator.api;

import dev.tanguy.game.jeopardy.common.domain.model.id.CategoryId;
import dev.tanguy.game.jeopardy.common.domain.model.id.QuestionId;
import dev.tanguy.game.jeopardy.common.domain.model.id.QuizId;

import java.util.List;

public record QuizSnapshot(QuizId id, String name, List<CategorySnapshot> categories) {

    public record CategorySnapshot(CategoryId id, String name, List<QuestionSnapshot> questions) {}

    public record QuestionSnapshot(QuestionId id, int points, String text, String answer, boolean dailyDouble) {}
}