package dev.tanguy.game.jeopardy.gameplay.domain.exception.player;

import dev.tanguy.game.jeopardy.common.domain.exception.DomainConflictException;

public class TeamAssignmentRequiredException extends DomainConflictException {
    public TeamAssignmentRequiredException() {
        super("This is a team game — players must join with a team assignment.");
    }
}