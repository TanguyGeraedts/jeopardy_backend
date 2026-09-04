package dev.tanguy.game.jeopardy.common.infrastructure.events;

import dev.tanguy.game.jeopardy.common.events.DomainEvent;
import dev.tanguy.game.jeopardy.common.events.DomainEventPublisher;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SpringDomainEventPublisher implements DomainEventPublisher {

    private final ApplicationEventPublisher springPublisher;

    public SpringDomainEventPublisher(ApplicationEventPublisher springPublisher) {
        this.springPublisher = springPublisher;
    }

    @Override
    public void publishAll(List<? extends DomainEvent> events) {
        events.forEach(springPublisher::publishEvent);
    }
}