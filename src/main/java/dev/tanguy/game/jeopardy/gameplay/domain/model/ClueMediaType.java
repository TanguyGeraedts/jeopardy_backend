package dev.tanguy.game.jeopardy.gameplay.domain.model;

/** Media shown with a clue's question. VIDEO is a YouTube link or a direct video file URL. */
public enum ClueMediaType {
    IMAGE,
    VIDEO,
    /** Sound only (an audio file, or a video file with its picture hidden). Never YouTube. */
    AUDIO
}
