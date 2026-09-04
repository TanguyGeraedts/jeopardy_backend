package dev.tanguy.game.jeopardy.common.adapter.in.web.exception;

import dev.tanguy.game.jeopardy.common.domain.exception.DomainConflictException;
import dev.tanguy.game.jeopardy.common.domain.exception.InvalidValueException;
import dev.tanguy.game.jeopardy.common.domain.exception.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final URI BLANK_TYPE = URI.create("about:blank");

    // --- 404 NOT FOUND ---
    @ExceptionHandler(NotFoundException.class)
    public ProblemDetail handleNotFound(NotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Resource Not Found");
        problem.setType(BLANK_TYPE);
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    // --- 409 CONFLICT ---
    @ExceptionHandler(DomainConflictException.class)
    public ProblemDetail handleDomainConflict(DomainConflictException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Domain Invariant Conflict");
        problem.setType(BLANK_TYPE);
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    // --- 400 BAD REQUEST ---
    @ExceptionHandler({
            InvalidValueException.class,
            IllegalArgumentException.class
    })
    public ProblemDetail handleBadRequest(RuntimeException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setTitle("Invalid Request Parameters");
        problem.setType(BLANK_TYPE);
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    // --- 500 INTERNAL SERVER ERROR ---
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneralException(Exception ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred."
        );
        problem.setTitle("Internal Server Error");
        problem.setType(BLANK_TYPE);
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }
}