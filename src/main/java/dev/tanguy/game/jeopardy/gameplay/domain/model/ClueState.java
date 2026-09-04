package dev.tanguy.game.jeopardy.gameplay.domain.model;

import dev.tanguy.game.jeopardy.common.domain.model.id.ClueId;
import lombok.Getter;

@Getter
public class ClueState {
    private final ClueId id;
    private final int value;
    private final String question;
    private final String answer;
    private final boolean isDailyDouble;
    private boolean isRevealed;

    public ClueState(ClueId id, int value, String question, String answer, boolean isDailyDouble) {
        this.id = id;
        this.value = value;
        this.question = question;
        this.answer = answer;
        this.isDailyDouble = isDailyDouble;
        this.isRevealed = false;
    }

    public void markAsRevealed() {
        this.isRevealed = true;
    }
}