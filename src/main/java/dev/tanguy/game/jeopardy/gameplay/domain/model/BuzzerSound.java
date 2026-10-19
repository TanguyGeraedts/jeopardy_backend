package dev.tanguy.game.jeopardy.gameplay.domain.model;

/**
 * The built-in buzzer sounds a team can have. The board synthesizes them in the browser (no audio files), so the
 * frontend must know every name here. A team without a choice gets one automatically by its position.
 */
public enum BuzzerSound {
    CLASSIC,
    BELL,
    HORN,
    LASER,
    CHIME,
    ARCADE,
    BOING,
    GONG
}
