package dev.tanguy.game.jeopardy.gameplay.port.in.session;

import dev.tanguy.game.jeopardy.common.domain.model.id.TeamId;

public interface CreateTeamUseCase {
    TeamId createTeam(CreateTeamCommand command);
}