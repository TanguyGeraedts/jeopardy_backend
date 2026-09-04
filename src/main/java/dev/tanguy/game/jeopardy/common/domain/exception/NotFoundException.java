package dev.tanguy.game.jeopardy.common.domain.exception;

public abstract class NotFoundException extends DomainException {

    protected NotFoundException(String message) {
        super(message);
    }
}