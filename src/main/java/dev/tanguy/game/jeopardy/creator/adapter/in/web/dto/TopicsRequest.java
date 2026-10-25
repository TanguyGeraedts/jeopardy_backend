package dev.tanguy.game.jeopardy.creator.adapter.in.web.dto;

import dev.tanguy.game.jeopardy.creator.domain.model.QuizTopic;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/** Replaces the quiz's topics. At most 3 (the Quiz checks); an empty list clears them. */
public record TopicsRequest(@NotNull List<@NotNull QuizTopic> topics) {}
