package dev.tanguy.game.jeopardy.gameplay.domain.model;

/**
 * The built-in buzzer sounds a team can have. The board synthesizes them in the browser (no audio files), so every
 * name here must also be in BUZZER_SOUND_IDS in jeopardy_frontend src/lib/buzzer-sounds.ts (kept in sync by hand;
 * the board warns in the console about a name it doesn't know). A team without a choice gets one by its position.
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
