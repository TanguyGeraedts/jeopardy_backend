package dev.tanguy.game.jeopardy.gameplay.port.in.session;

import dev.tanguy.game.jeopardy.gameplay.domain.model.GameMode;

import java.util.Objects;

public record CreateGameSessionCommand(
        String boardId,
        GameMode mode
) {
    public CreateGameSessionCommand {
        Objects.requireNonNull(boardId, "boardId cannot be null");
        if (boardId.isBlank()) {
            throw new IllegalArgumentException("boardId cannot be blank");
        }
    }
}