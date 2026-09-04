package dev.tanguy.game.jeopardy.gameplay.domain.exception.player;

import dev.tanguy.game.jeopardy.common.domain.exception.DomainConflictException;
import dev.tanguy.game.jeopardy.common.domain.model.id.TeamId;

public class UnexpectedTeamAssignmentException extends DomainConflictException {
    public UnexpectedTeamAssignmentException(TeamId teamId) {
        super("This is a solo game — players cannot join with a team assignment (got team " + teamId.value() + ").");
    }
}