package dev.tanguy.game.jeopardy.gameplay.port.in.session;

import dev.tanguy.game.jeopardy.common.domain.model.id.GameSessionId;
import dev.tanguy.game.jeopardy.common.domain.model.id.OwnerId;
import dev.tanguy.game.jeopardy.common.domain.model.id.PlayerId;

/** The host adds a player by hand (players normally come in through the lobby handoff). Owner only. */
public record AddPlayerCommand(
        GameSessionId sessionId,
        OwnerId requesterId,
        PlayerId playerId,
        String playerName,
        String externalTeamId,
        String teamName,
        String teamColour
) {}
