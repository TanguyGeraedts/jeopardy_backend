package dev.tanguy.game.jeopardy.gameplay.adapter.in.web.dto;

import java.util.List;
import java.util.UUID;

/**
 * The whole game as screens see it. The host version has the answers; the public version
 * (live stream, phones) leaves them out and only shows the question of the open clue.
 */
public record GameSessionResponse(
        UUID id,
        String lobbyCode,
        String state,
        String mode,
        /** The open clue, or null on the board. */
        String activeClueId,
        /** The team that picks the next clue. */
        String activeTeamId,
        /** Who is answering right now (first in the buzz queue). Null unless ANSWER_EVALUATION. */
        String answeringPlayerId,
        /** Buzzes on the open clue, in arrival order. */
        List<BuzzSummary> buzzes,
        List<ClueSummary> clues,
        List<TeamSummary> teams,
        List<PlayerSummary> players
) {
    public record ClueSummary(
            String id, String category, int value, String question, boolean revealed, boolean dailyDouble, String answer,
            /** "IMAGE" or "VIDEO" (a YouTube link or a direct video file), shown with the question. Null = text only. */
            String mediaType,
            String mediaUrl
    ) {}

    public record TeamSummary(
            String id, String name, String externalTeamId, String colour, int score, List<String> memberIds,
            /** Answered the open clue wrongly. */
            boolean lockedOut,
            /** Played on the board when the team buzzes. Null = automatic (by team position). */
            String buzzerSound,
            /** A buzz from this team would count right now. */
            boolean canBuzz
    ) {}

    public record PlayerSummary(String id, String name, String teamId) {}

    public record BuzzSummary(String playerId, String teamId) {}
}
