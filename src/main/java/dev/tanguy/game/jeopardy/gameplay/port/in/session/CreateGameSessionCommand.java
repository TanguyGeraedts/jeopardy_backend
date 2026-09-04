package dev.tanguy.game.jeopardy.gameplay.port.in.session;

import java.util.Objects;

public record CreateGameSessionCommand(
        String boardId
) {
    public CreateGameSessionCommand {
        Objects.requireNonNull(boardId, "boardId cannot be null");
        if (boardId.isBlank()) {
            throw new IllegalArgumentException("boardId cannot be blank");
        }
    }
}