package dev.tanguy.game.jeopardy.common.domain.exception;

/** A downstream service (e.g. the lobby microservice) failed or returned something unusable. Maps to 502. */
public class ExternalServiceException extends DomainException {

    public ExternalServiceException(String message) {
        super(message);
    }

    public ExternalServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}