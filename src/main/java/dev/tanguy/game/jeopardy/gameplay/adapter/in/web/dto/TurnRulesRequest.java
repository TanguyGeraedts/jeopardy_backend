package dev.tanguy.game.jeopardy.gameplay.adapter.in.web.dto;

import dev.tanguy.game.jeopardy.gameplay.domain.model.TurnRules;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/** Taking turns on/off, and the turn team's head start in seconds (default 7). */
public record TurnRulesRequest(
        boolean enabled,
        @Min(value = TurnRules.MIN_HEAD_START, message = "The head start must be at least 5 seconds")
        @Max(value = TurnRules.MAX_HEAD_START, message = "The head start can be at most 10 seconds")
        Integer headStartSeconds
) {
    public TurnRules toRules() {
        return new TurnRules(enabled, headStartSeconds == null ? TurnRules.DEFAULT_HEAD_START : headStartSeconds);
    }
}
