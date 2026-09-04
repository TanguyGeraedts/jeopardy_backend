package dev.tanguy.game.jeopardy.common.domain.exception;

public abstract class InvalidValueException extends DomainException {

    protected InvalidValueException(String message) {
        super(message);
    }

    protected InvalidValueException(String message, Throwable cause) {
        super(message, cause);
    }
}