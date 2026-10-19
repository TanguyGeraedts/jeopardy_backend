package dev.tanguy.game.jeopardy.gameplay.domain.exception.player;

import dev.tanguy.game.jeopardy.common.domain.exception.InvalidValueException;

/** The player token is missing, forged, expired, or belongs to another game. */
public class InvalidPlayerTokenException extends InvalidValueException {

    public InvalidPlayerTokenException(String message) {
        super(message);
    }

    public InvalidPlayerTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
