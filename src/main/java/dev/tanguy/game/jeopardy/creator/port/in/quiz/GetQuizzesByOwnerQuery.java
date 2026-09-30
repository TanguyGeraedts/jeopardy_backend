package dev.tanguy.game.jeopardy.creator.port.in.quiz;

import dev.tanguy.game.jeopardy.common.domain.model.id.OwnerId;

import java.util.Objects;

public record GetQuizzesByOwnerQuery(
        OwnerId ownerId
) {
    public GetQuizzesByOwnerQuery {
        Objects.requireNonNull(ownerId, "ownerId cannot be null");
    }
}