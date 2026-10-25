package dev.tanguy.game.jeopardy.gameplay.adapter.in.web.mapper;

import dev.tanguy.game.jeopardy.common.domain.model.id.PlayerId;
import dev.tanguy.game.jeopardy.common.domain.model.id.TeamId;
import dev.tanguy.game.jeopardy.gameplay.adapter.in.web.dto.GameSessionResponse;
import dev.tanguy.game.jeopardy.gameplay.adapter.in.web.dto.GameSessionResponse.BuzzSummary;
import dev.tanguy.game.jeopardy.gameplay.adapter.in.web.dto.GameSessionResponse.ClueSummary;
import dev.tanguy.game.jeopardy.gameplay.adapter.in.web.dto.GameSessionResponse.PlayerSummary;
import dev.tanguy.game.jeopardy.gameplay.adapter.in.web.dto.GameSessionResponse.TeamSummary;
import dev.tanguy.game.jeopardy.gameplay.domain.model.ClueState;
import dev.tanguy.game.jeopardy.gameplay.domain.model.GameSession;
import dev.tanguy.game.jeopardy.gameplay.domain.model.GameState;
import dev.tanguy.game.jeopardy.gameplay.domain.model.Player;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class GameSessionResponseMapper {

    private GameSessionResponseMapper() {}

    /** For the host's screens: everything, including answers of revealed clues. */
    public static GameSessionResponse toResponse(GameSession session) {
        return map(session, true);
    }

    /** For anyone (live stream, phones): no answers, and only the open clue's question. */
    public static GameSessionResponse toPublicResponse(GameSession session) {
        return map(session, false);
    }

    private static GameSessionResponse map(GameSession session, boolean forHost) {
        List<ClueSummary> clues = session.getClues().values().stream()
                .map(clue -> forHost ? toClueSummary(clue) : toPublicClueSummary(clue, session))
                .toList();

        boolean buzzing = session.getState() == GameState.BUZZER_OPEN || session.getState() == GameState.ANSWER_EVALUATION;

        List<TeamSummary> teams = session.getTeams().stream()
                .map(t -> new TeamSummary(
                        t.getId().value().toString(),
                        t.getName(),
                        t.getExternalTeamId(),
                        t.getColour(),
                        t.getScore(),
                        t.getMemberIds().stream().map(id -> id.value().toString()).toList(),
                        t.isLockedOut(),
                        t.getBuzzerSound() != null ? t.getBuzzerSound().name() : null,
                        buzzing && session.canBuzz(t.getId())))
                .toList();

        List<PlayerSummary> players = session.getPlayers().stream()
                .map(p -> new PlayerSummary(
                        p.id().value().toString(),
                        p.name(),
                        p.teamId() != null ? p.teamId().value().toString() : null))
                .toList();

        Instant openedAt = session.getBuzzersOpenedAt();
        List<BuzzSummary> buzzes = session.getBuzzes().stream()
                .map(buzz -> {
                    TeamId teamId = session.findPlayer(buzz.playerId()).map(Player::teamId).orElse(null);
                    boolean lockedOut = session.getTeams().stream().anyMatch(t -> t.getId().equals(teamId) && t.isLockedOut());
                    return new BuzzSummary(
                            buzz.playerId().value().toString(),
                            teamId != null ? teamId.value().toString() : null,
                            openedAt != null ? Duration.between(openedAt, buzz.at()).toMillis() : 0,
                            lockedOut);
                })
                .toList();

        return new GameSessionResponse(
                session.getId().value(),
                session.getLobbyCode(),
                session.getState().name(),
                session.getMode().name(),
                session.getActiveClueId() != null ? session.getActiveClueId().value() : null,
                session.getActiveTeamId() != null ? session.getActiveTeamId().value().toString() : null,
                idOf(session.getCurrentBuzzedPlayerId()),
                buzzes,
                clues, teams, players);
    }

    private static String idOf(PlayerId playerId) {
        UUID value = playerId == null ? null : playerId.value();
        return value == null ? null : value.toString();
    }

    private static ClueSummary toClueSummary(ClueState clue) {
        return new ClueSummary(
                clue.getId().value(),
                clue.getCategoryName(),
                clue.getValue(),
                clue.getQuestion(),
                clue.isRevealed(),
                clue.isDailyDouble(),
                clue.isRevealed() ? clue.getAnswer() : null,
                clue.getMediaType() != null ? clue.getMediaType().name() : null,
                clue.getMediaUrl()
        );
    }

    private static ClueSummary toPublicClueSummary(ClueState clue, GameSession session) {
        boolean open = clue.getId().equals(session.getActiveClueId());
        return new ClueSummary(
                clue.getId().value(),
                clue.getCategoryName(),
                clue.getValue(),
                open ? clue.getQuestion() : null,
                clue.isRevealed(),
                clue.isDailyDouble(),
                null,
                open && clue.getMediaType() != null ? clue.getMediaType().name() : null,
                open ? clue.getMediaUrl() : null
        );
    }
}
