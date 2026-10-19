package dev.tanguy.game.jeopardy.gameplay.adapter.in.web.dto;

/** Send exactly one: delta adds to the score, score replaces it. */
public record ChangeScoreRequest(Integer delta, Integer score) {}
