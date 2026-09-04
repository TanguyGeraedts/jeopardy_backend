package dev.tanguy.game.jeopardy.gameplay.domain.exception.player;

import dev.tanguy.game.jeopardy.common.domain.exception.DomainConflictException;
import dev.tanguy.game.jeopardy.common.domain.model.id.PlayerId;

public class InvalidTurnException extends DomainConflictException {

    public InvalidTurnException(PlayerId playerId) {
        super("Player with ID " + playerId.value() + " attempted an action out of turn");
    }
}