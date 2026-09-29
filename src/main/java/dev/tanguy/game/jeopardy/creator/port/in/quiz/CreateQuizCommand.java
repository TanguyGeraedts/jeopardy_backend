package dev.tanguy.game.jeopardy.creator.port.in.quiz;

import dev.tanguy.game.jeopardy.common.domain.model.id.OwnerId;

import java.util.Objects;

public record CreateQuizCommand(
        OwnerId ownerId,
        String name
) {
    public CreateQuizCommand {
        Objects.requireNonNull(ownerId, "ownerId cannot be null");
        Objects.requireNonNull(name, "name cannot be null");
        name = name.strip();
        if (name.isEmpty()) {
            throw new IllegalArgumentException("name cannot be blank");
        }
    }
}