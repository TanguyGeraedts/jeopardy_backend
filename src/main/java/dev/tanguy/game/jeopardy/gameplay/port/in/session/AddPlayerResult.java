package dev.tanguy.game.jeopardy.gameplay.port.in.session;

import dev.tanguy.game.jeopardy.common.domain.model.id.PlayerId;
import dev.tanguy.game.jeopardy.common.domain.model.id.TeamId;

public record AddPlayerResult(
        PlayerId playerId,
        TeamId teamId,
        boolean alreadyJoined
) {}