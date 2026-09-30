package dev.tanguy.game.jeopardy.creator.adapter.in.web.dto;

import dev.tanguy.game.jeopardy.creator.domain.model.AnswerType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** Body for both creating and replacing a question. */
public record QuestionRequest(
        // Boxed on purpose: Jackson 3 rejects a missing primitive record component as a
        // malformed body, which would hide our validation messages and break optional fields.
        @NotNull(message = "Points are required")
        @Positive(message = "Points must be greater than 0")
        Integer points,

        @NotBlank(message = "Question text is required")
        @Size(max = 2000, message = "Question text must be at most 2000 characters")
        String questionText,

        @NotBlank(message = "Answer text is required")
        @Size(max = 1000, message = "Answer text must be at most 1000 characters")
        String answerText,

        @NotNull(message = "Answer type is required")
        AnswerType answerType,

        // Only http(s): the URL ends up in front of players, so no javascript:/data: schemes.
        @Size(max = 2048, message = "Media URL must be at most 2048 characters")
        @Pattern(regexp = "^https?://\\S+$", message = "Media URL must be a valid http(s) URL")
        String mediaUrl,

        // Optional: an absent value means "not a daily double".
        Boolean dailyDouble
) {
    public QuestionRequest {
        questionText = questionText == null ? null : questionText.trim();
        answerText = answerText == null ? null : answerText.trim();
        mediaUrl = mediaUrl == null || mediaUrl.isBlank() ? null : mediaUrl.trim();
        dailyDouble = Boolean.TRUE.equals(dailyDouble);
    }
}