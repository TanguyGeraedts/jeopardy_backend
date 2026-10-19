package dev.tanguy.game.jeopardy.gameplay.adapter.in.web.dto;

/** accepted = false when the buzz didn't count (closed, locked out, or the team already buzzed). */
public record BuzzResponse(boolean accepted) {}
