package dev.tanguy.game.jeopardy.gameplay.port.in.session;

import dev.tanguy.game.jeopardy.common.domain.model.id.GameSessionId;

/** playerToken = the lobby-signed game token; it says who is buzzing. */
public record BuzzCommand(GameSessionId sessionId, String playerToken) {}
