package dev.tanguy.game.jeopardy.gameplay.port.in.session;

import dev.tanguy.game.jeopardy.common.domain.model.id.GameSessionId;
import dev.tanguy.game.jeopardy.common.domain.model.id.PlayerId;

public record AddPlayerCommand(
        GameSessionId sessionId,
        PlayerId playerId,
        String playerName,
        String externalTeamId,
        String teamName,
        String teamColour
) {}