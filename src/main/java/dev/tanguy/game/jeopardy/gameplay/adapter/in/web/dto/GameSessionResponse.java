package dev.tanguy.game.jeopardy.gameplay.adapter.in.web.dto;

import java.util.List;
import java.util.UUID;

public record GameSessionResponse(
        UUID id,
        String state,
        List<ClueSummary> clues,
        List<TeamSummary> teams,
        List<PlayerSummary> players
) {
    public record ClueSummary(
            String id, int value, String question, boolean revealed, boolean dailyDouble, String answer
    ) {}

    public record TeamSummary(String id, String name, int score, List<String> memberIds) {}

    public record PlayerSummary(String id, String name, String teamId) {}
}