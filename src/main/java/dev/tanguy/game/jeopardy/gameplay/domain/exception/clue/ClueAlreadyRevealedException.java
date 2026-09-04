package dev.tanguy.game.jeopardy.gameplay.domain.exception.clue;

import dev.tanguy.game.jeopardy.common.domain.exception.DomainConflictException;
import dev.tanguy.game.jeopardy.common.domain.model.id.ClueId;

public class ClueAlreadyRevealedException extends DomainConflictException {

    public ClueAlreadyRevealedException(ClueId clueId) {
        super("Clue with ID " + clueId.value() + " has already been selected");
    }
}