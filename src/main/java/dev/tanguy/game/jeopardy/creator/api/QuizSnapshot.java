package dev.tanguy.game.jeopardy.creator.api;

import dev.tanguy.game.jeopardy.common.domain.model.id.CategoryId;
import dev.tanguy.game.jeopardy.common.domain.model.id.QuestionId;
import dev.tanguy.game.jeopardy.common.domain.model.id.QuizId;

import java.util.List;

public record QuizSnapshot(QuizId id, String name, List<CategorySnapshot> categories) {

    public record CategorySnapshot(CategoryId id, String name, List<QuestionSnapshot> questions) {}

    /** mediaType is "IMAGE", "VIDEO" or "AUDIO" (an AnswerType name) with a mediaUrl, or both null for a text-only question. */
    public record QuestionSnapshot(
            QuestionId id, int points, String text, String answer, boolean dailyDouble, String mediaType, String mediaUrl) {}
}
