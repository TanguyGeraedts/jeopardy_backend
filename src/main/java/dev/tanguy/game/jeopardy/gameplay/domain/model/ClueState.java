package dev.tanguy.game.jeopardy.gameplay.domain.model;

import dev.tanguy.game.jeopardy.common.domain.model.id.ClueId;
import lombok.Getter;

@Getter
public class ClueState {
    private final ClueId id;
    private final String categoryName;
    private final int value;
    private final String question;
    private final String answer;
    private final boolean isDailyDouble;
    /** Shown together with the question (e.g. "Who is this?" + photo). Both null for a text-only clue. */
    private final ClueMediaType mediaType;
    private final String mediaUrl;
    private boolean isRevealed;

    public ClueState(ClueId id, String categoryName, int value, String question, String answer, boolean isDailyDouble) {
        this(id, categoryName, value, question, answer, isDailyDouble, null, null);
    }

    public ClueState(ClueId id, String categoryName, int value, String question, String answer, boolean isDailyDouble,
                     ClueMediaType mediaType, String mediaUrl) {
        this.id = id;
        this.categoryName = categoryName;
        this.value = value;
        this.question = question;
        this.answer = answer;
        this.isDailyDouble = isDailyDouble;
        // Media without a URL (or the other way round) is meaningless: keep both or neither.
        boolean hasMedia = mediaType != null && mediaUrl != null && !mediaUrl.isBlank();
        this.mediaType = hasMedia ? mediaType : null;
        this.mediaUrl = hasMedia ? mediaUrl : null;
        this.isRevealed = false;
    }

    public void markAsRevealed() {
        this.isRevealed = true;
    }
}
