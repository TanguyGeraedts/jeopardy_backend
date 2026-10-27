package dev.tanguy.game.jeopardy.gameplay.domain.model;

/** What happened to a buzz. */
public enum BuzzOutcome {
    /** In line (or answering). */
    ACCEPTED,
    /** Before the buzzers opened, or during another team's head start: the player is locked out for a moment. */
    TOO_EARLY,
    /** Doesn't count, no penalty: no clue open, team locked out or already in line, or someone else's daily double. */
    IGNORED
}
