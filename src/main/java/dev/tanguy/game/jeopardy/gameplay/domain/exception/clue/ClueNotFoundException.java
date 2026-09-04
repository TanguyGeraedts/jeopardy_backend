package dev.tanguy.game.jeopardy.gameplay.domain.exception.clue;

import dev.tanguy.game.jeopardy.common.domain.exception.NotFoundException;
import dev.tanguy.game.jeopardy.common.domain.model.id.ClueId;

public class ClueNotFoundException extends NotFoundException {
    public ClueNotFoundException(ClueId id) {
        super("Clue not found with ID: " + id.value());
    }
}