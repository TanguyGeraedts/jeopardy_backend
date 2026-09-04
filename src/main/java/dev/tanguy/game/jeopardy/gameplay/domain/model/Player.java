package dev.tanguy.game.jeopardy.gameplay.domain.model;

import dev.tanguy.game.jeopardy.common.domain.model.id.PlayerId;
import dev.tanguy.game.jeopardy.common.domain.model.id.TeamId;

import java.util.Optional;

public record Player(
        PlayerId id,
        String name,
        TeamId teamId
) {
    public Optional<TeamId> getTeamId() {
        return Optional.ofNullable(teamId);
    }
}