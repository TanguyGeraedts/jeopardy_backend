package dev.tanguy.game.jeopardy.gameplay.port.out.lobby;

import java.util.UUID;

/** Who a lobby-signed player token belongs to. */
public record LobbyPlayerIdentity(String lobbyCode, UUID playerId) {}
