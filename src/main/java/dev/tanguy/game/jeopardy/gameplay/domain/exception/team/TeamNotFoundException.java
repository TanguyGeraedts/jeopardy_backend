package dev.tanguy.game.jeopardy.gameplay.domain.exception.team;

import dev.tanguy.game.jeopardy.common.domain.exception.NotFoundException;
import dev.tanguy.game.jeopardy.common.domain.model.id.TeamId;

public class TeamNotFoundException extends NotFoundException {
    public TeamNotFoundException(TeamId id) {
        super("Team not found with ID: " + id.value());
    }
}