package dev.tanguy.game.jeopardy.common.events;

import dev.tanguy.game.jeopardy.common.domain.model.id.OwnerId;

import java.time.Instant;
import java.util.Objects;

/**
 * A Clerk account was deleted. Every module removes what that owner had (privacy notice: deleting your account
 * deletes your quizzes). Handlers must be idempotent: Clerk retries a webhook until it gets a 2xx.
 */
public record UserAccountDeletedEvent(OwnerId ownerId, Instant occurredAt) implements DomainEvent {

    public UserAccountDeletedEvent {
        Objects.requireNonNull(ownerId, "ownerId cannot be null");
        Objects.requireNonNull(occurredAt, "occurredAt cannot be null");
    }
}
