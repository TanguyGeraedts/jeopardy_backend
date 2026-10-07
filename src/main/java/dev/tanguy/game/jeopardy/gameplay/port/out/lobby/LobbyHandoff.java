package dev.tanguy.game.jeopardy.gameplay.port.out.lobby;

import java.util.List;
import java.util.UUID;

/** What the lobby tells us when its host starts the game: who is in the room and in which team. */
public record LobbyHandoff(String lobbyCode, List<Player> players, List<Team> teams) {

    public LobbyHandoff {
        players = players == null ? List.of() : List.copyOf(players);
        teams = teams == null ? List.of() : List.copyOf(teams);
    }

    /** teamId is null in solo lobbies. Every lobby player plays; the quiz master never joins the lobby. */
    public record Player(UUID playerId, String username, Integer teamId) {}

    public record Team(int teamId, String name, String colour) {}
}