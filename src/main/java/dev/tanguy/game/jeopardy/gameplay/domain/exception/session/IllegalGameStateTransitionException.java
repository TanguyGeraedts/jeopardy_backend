package dev.tanguy.game.jeopardy.gameplay.domain.exception.session;

import dev.tanguy.game.jeopardy.common.domain.exception.DomainConflictException;
import dev.tanguy.game.jeopardy.gameplay.domain.model.GameState;

public class IllegalGameStateTransitionException extends DomainConflictException {

    public IllegalGameStateTransitionException(GameState current, String action) {
        super("Cannot perform action '" + action + "' while game state is " + current);
    }
}