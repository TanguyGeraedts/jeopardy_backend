package dev.tanguy.game.jeopardy.creator.domain.model;

/** What is shown with the question. The name predates that: it is the question's media, not the answer's. */
public enum AnswerType {
    TEXT,
    IMAGE,
    /** Full video: a YouTube link or a video file. */
    VIDEO,
    /** Sound only, nothing on screen: an audio file, or a video file whose picture is hidden. Not YouTube. */
    AUDIO
}
