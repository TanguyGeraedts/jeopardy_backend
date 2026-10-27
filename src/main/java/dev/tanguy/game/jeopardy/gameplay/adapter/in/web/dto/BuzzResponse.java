package dev.tanguy.game.jeopardy.gameplay.adapter.in.web.dto;

/**
 * accepted = in line. result: ACCEPTED, TOO_EARLY (locked out for lockedOutMs: pressed before the buzzers opened or
 * during another team's head start) or IGNORED (doesn't count, no penalty).
 */
public record BuzzResponse(boolean accepted, String result, long lockedOutMs) {}
