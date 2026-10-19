package dev.tanguy.game.jeopardy.creator.domain.event.question;

import dev.tanguy.game.jeopardy.common.domain.exception.InvalidValueException;

/** YouTube's embed policy doesn't allow playing a video's sound with the picture hidden. */
public class UnsupportedAudioUrlException extends InvalidValueException {

    public UnsupportedAudioUrlException() {
        super("Sound-only questions need a direct audio or video file link; YouTube links can only be used as a full video.");
    }
}
