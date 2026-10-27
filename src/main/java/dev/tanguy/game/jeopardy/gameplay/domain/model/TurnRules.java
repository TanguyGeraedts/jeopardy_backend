package dev.tanguy.game.jeopardy.gameplay.domain.model;

/**
 * Taking turns: each clue one team (in rotation) gets a head start to buzz, then anyone may steal.
 * Off = everyone buzzes at once, and the buzzers open after a short random delay instead.
 */
public record TurnRules(boolean enabled, int headStartSeconds) {

    public static final int MIN_HEAD_START = 5;
    public static final int MAX_HEAD_START = 10;
    public static final int DEFAULT_HEAD_START = 7;
    public static final TurnRules OFF = new TurnRules(false, DEFAULT_HEAD_START);

    public TurnRules {
        if (headStartSeconds < MIN_HEAD_START || headStartSeconds > MAX_HEAD_START) {
            throw new IllegalArgumentException(
                    "The head start must be " + MIN_HEAD_START + " to " + MAX_HEAD_START + " seconds, not " + headStartSeconds);
        }
    }
}
