package dev.tanguy.game.jeopardy.common.events;

import java.util.List;

public interface DomainEventPublisher {
    void publishAll(List<? extends DomainEvent> events);

    default void publish(DomainEvent event) {
        publishAll(List.of(event));
    }
}