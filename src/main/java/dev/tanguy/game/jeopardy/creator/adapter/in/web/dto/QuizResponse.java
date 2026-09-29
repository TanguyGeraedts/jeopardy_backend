package dev.tanguy.game.jeopardy.creator.adapter.in.web.dto;

import java.util.List;
import java.util.UUID;

public record QuizResponse(
        UUID id,
        String name,
        List<CategoryResponse> categories
) {

    public record CategoryResponse(
            UUID id,
            String name,
            List<QuestionResponse> questions
    ) {}

    public record QuestionResponse(
            UUID id,
            int points,
            String questionText,
            String answerText,
            String answerType,
            String mediaUrl,
            boolean dailyDouble
    ) {}
}