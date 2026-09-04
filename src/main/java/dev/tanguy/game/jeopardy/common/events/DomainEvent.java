package dev.tanguy.game.jeopardy.common.events;

import java.time.Instant;

public interface DomainEvent {
    Instant occurredAt();
}