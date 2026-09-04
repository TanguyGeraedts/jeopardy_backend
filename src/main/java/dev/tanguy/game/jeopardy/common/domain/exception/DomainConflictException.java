package dev.tanguy.game.jeopardy.common.domain.exception;

public abstract class DomainConflictException extends DomainException {

    protected DomainConflictException(String message) {
        super(message);
    }
}