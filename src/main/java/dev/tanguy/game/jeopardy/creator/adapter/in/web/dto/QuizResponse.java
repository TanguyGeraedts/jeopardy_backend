package dev.tanguy.game.jeopardy.creator.adapter.in.web.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** The owner's view of a quiz: everything, answers included. Others only ever get a DiscoverQuizResponse. */
public record QuizResponse(
        UUID id,
        String name,
        List<CategoryResponse> categories,
        /** Findable and hostable by anyone signed in. */
        boolean published,
        Instant publishedAt,
        String authorName,
        /** QuizTopic names, at most 3. */
        List<String> topics,
        /** Games hosted with this quiz, by anyone. */
        int timesPlayed,
        Instant updatedAt
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
