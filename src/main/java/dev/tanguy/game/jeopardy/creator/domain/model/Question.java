package dev.tanguy.game.jeopardy.creator.domain.model;

import dev.tanguy.game.jeopardy.common.domain.model.id.CategoryId;
import dev.tanguy.game.jeopardy.common.domain.model.id.QuestionId;
import dev.tanguy.game.jeopardy.creator.domain.event.question.InvalidAnswerTextException;
import dev.tanguy.game.jeopardy.creator.domain.event.question.InvalidPointsException;
import dev.tanguy.game.jeopardy.creator.domain.event.question.InvalidQuestionTextException;
import dev.tanguy.game.jeopardy.creator.domain.event.question.MissingMediaUrlException;
import dev.tanguy.game.jeopardy.creator.domain.event.question.UnsupportedAudioUrlException;
import lombok.Getter;
import lombok.Setter;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
public class Question {

    private final QuestionId id;
    private final CategoryId categoryId;
    private int points;
    private String questionText;
    private String answerText;
    private AnswerType answerType;
    private String mediaUrl;
    @Setter
    private boolean isDailyDouble;

    public Question(
            QuestionId id,
            CategoryId categoryId,
            int points,
            String questionText,
            String answerText,
            AnswerType answerType,
            String mediaUrl
    ) {
        this(id, categoryId, points, questionText, answerText, answerType, mediaUrl, false);
    }

    public Question(
            QuestionId id,
            CategoryId categoryId,
            int points,
            String questionText,
            String answerText,
            AnswerType answerType,
            String mediaUrl,
            boolean isDailyDouble
    ) {
        this.id = Objects.requireNonNull(id, "QuestionId cannot be null");
        this.categoryId = Objects.requireNonNull(categoryId, "CategoryId cannot be null");

        setPoints(points);
        setQuestionText(questionText);
        setAnswer(answerText, answerType, mediaUrl);
        this.isDailyDouble = isDailyDouble;
    }

    public void setPoints(int points) {
        if (points <= 0) {
            throw new InvalidPointsException(this.id, points);
        }
        this.points = points;
    }

    public void setQuestionText(String questionText) {
        if (questionText == null || questionText.isBlank()) {
            throw new InvalidQuestionTextException(this.id);
        }
        this.questionText = questionText;
    }

    public void setAnswerText(String answerText) {
        if (answerText == null || answerText.isBlank()) {
            throw new InvalidAnswerTextException(this.id);
        }
        this.answerText = answerText;
    }

    public void setAnswer(String answerText, AnswerType answerType, String mediaUrl) {
        setAnswerText(answerText);
        setMedia(answerType, mediaUrl);
    }

    public void setMedia(AnswerType answerType, String mediaUrl) {
        AnswerType resolvedType = Objects.requireNonNull(answerType, "AnswerType cannot be null");
        if (resolvedType != AnswerType.TEXT && (mediaUrl == null || mediaUrl.isBlank())) {
            throw new MissingMediaUrlException(this.id, resolvedType);
        }
        if (resolvedType == AnswerType.AUDIO && isYoutube(mediaUrl)) {
            throw new UnsupportedAudioUrlException();
        }
        this.answerType = resolvedType;
        this.mediaUrl = resolvedType == AnswerType.TEXT ? null : mediaUrl;
    }

    /**
     * Host part of a URL, read leniently: unlike URI.create it doesn't give up on spaces or other unencoded
     * characters, so such a link can't slip past the YouTube rule.
     */
    private static final Pattern URL_HOST = Pattern.compile("^\\s*[a-zA-Z][a-zA-Z0-9+.-]*://(?:[^@/?#]*@)?([^/?#:\\s]+)");

    private static boolean isYoutube(String url) {
        Matcher matcher = URL_HOST.matcher(url);
        if (!matcher.find()) {
            return false; // not a URL at all: the web layer's URL check reports that
        }
        // Hosts are case-insensitive, and a trailing dot ("youtube.com.") is the same host.
        String host = matcher.group(1).toLowerCase(Locale.ROOT).replaceAll("\\.$", "");
        return host.equals("youtu.be") || host.equals("youtube.com") || host.endsWith(".youtube.com")
                || host.equals("youtube-nocookie.com") || host.endsWith(".youtube-nocookie.com");
    }

    public void toggleDailyDouble() {
        this.isDailyDouble = !this.isDailyDouble;
    }
}