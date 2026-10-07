package dev.tanguy.game.jeopardy.gameplay.domain.exception.session;

import dev.tanguy.game.jeopardy.common.domain.exception.InvalidValueException;

public class InvalidHandoffException extends InvalidValueException {

    public InvalidHandoffException(String message) {
        super(message);
    }

    public InvalidHandoffException(String message, Throwable cause) {
        super(message, cause);
    }
}