package dev.tanguy.game.jeopardy.gameplay.adapter.in.web.mapper;

import dev.tanguy.game.jeopardy.gameplay.adapter.in.web.dto.GameSessionResponse;
import dev.tanguy.game.jeopardy.gameplay.adapter.in.web.dto.GameSessionResponse.ClueSummary;
import dev.tanguy.game.jeopardy.gameplay.adapter.in.web.dto.GameSessionResponse.PlayerSummary;
import dev.tanguy.game.jeopardy.gameplay.adapter.in.web.dto.GameSessionResponse.TeamSummary;
import dev.tanguy.game.jeopardy.gameplay.domain.model.ClueState;
import dev.tanguy.game.jeopardy.gameplay.domain.model.GameSession;

import java.util.List;

public final class GameSessionResponseMapper {

    private GameSessionResponseMapper() {}

    public static GameSessionResponse toResponse(GameSession session) {
        List<ClueSummary> clues = session.getClues().values().stream()
                .map(GameSessionResponseMapper::toClueSummary)
                .toList();

        List<TeamSummary> teams = session.getTeams().stream()
                .map(t -> new TeamSummary(
                        t.getId().value().toString(),
                        t.getName(),
                        t.getScore(),
                        t.getMemberIds().stream().map(id -> id.value().toString()).toList()))
                .toList();

        List<PlayerSummary> players = session.getPlayers().stream()
                .map(p -> new PlayerSummary(
                        p.id().value().toString(),
                        p.name(),
                        p.teamId() != null ? p.teamId().value().toString() : null))
                .toList();

        return new GameSessionResponse(session.getId().value(), session.getState().name(), clues, teams, players);
    }

    private static ClueSummary toClueSummary(ClueState clue) {
        return new ClueSummary(
                clue.getId().value(),
                clue.getValue(),
                clue.getQuestion(),
                clue.isRevealed(),
                clue.isDailyDouble(),
                clue.isRevealed() ? clue.getAnswer() : null
        );
    }
}