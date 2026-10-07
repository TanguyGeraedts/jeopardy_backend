package dev.tanguy.game.jeopardy.gameplay.adapter.out.persistence;

import dev.tanguy.game.jeopardy.common.domain.model.id.ClueId;
import dev.tanguy.game.jeopardy.common.domain.model.id.GameSessionId;
import dev.tanguy.game.jeopardy.common.domain.model.id.OwnerId;
import dev.tanguy.game.jeopardy.common.domain.model.id.PlayerId;
import dev.tanguy.game.jeopardy.common.domain.model.id.QuizId;
import dev.tanguy.game.jeopardy.common.domain.model.id.TeamId;
import dev.tanguy.game.jeopardy.gameplay.domain.model.ClueState;
import dev.tanguy.game.jeopardy.gameplay.domain.model.GameSession;
import dev.tanguy.game.jeopardy.gameplay.domain.model.Player;
import dev.tanguy.game.jeopardy.gameplay.domain.model.Team;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Maps between the GameSession aggregate and its JPA entities. */
@Component
public class GameSessionMapper {

    // ---------- domain -> JPA ----------

    public GameSessionJpaEntity toJpaEntity(GameSession session) {
        GameSessionJpaEntity entity = GameSessionJpaEntity.builder()
                .id(session.getId().value())
                .ownerId(session.getOwnerId().value())
                .quizId(UUID.fromString(session.getQuizId().value()))
                .lobbyCode(session.getLobbyCode())
                .mode(session.getMode())
                .state(session.getState())
                .activeTeamId(session.getActiveTeamId() == null ? null : session.getActiveTeamId().value())
                .currentBuzzedPlayerId(session.getCurrentBuzzedPlayerId() == null ? null : session.getCurrentBuzzedPlayerId().value())
                .activeClueId(session.getActiveClueId() == null ? null : UUID.fromString(session.getActiveClueId().value()))
                .build();

        int position = 0;
        for (ClueState clue : session.getClues().values()) {
            entity.addClue(ClueJpaEntity.builder()
                    .id(UUID.fromString(clue.getId().value()))
                    .sortOrder(position++)
                    .categoryName(clue.getCategoryName())
                    .points(clue.getValue())
                    .questionText(clue.getQuestion())
                    .answerText(clue.getAnswer())
                    .dailyDouble(clue.isDailyDouble())
                    .revealed(clue.isRevealed())
                    .build());
        }

        for (Team team : session.getTeams()) {
            entity.addTeam(TeamJpaEntity.builder()
                    .id(team.getId().value())
                    .name(team.getName())
                    .externalTeamId(team.getExternalTeamId())
                    .colour(team.getColour())
                    .score(team.getScore())
                    .build());
        }

        for (Player player : session.getPlayers()) {
            entity.addPlayer(PlayerJpaEntity.builder()
                    .id(PlayerJpaEntity.rowId(session.getId().value(), player.id().value()))
                    .playerId(player.id().value())
                    .name(player.name())
                    .teamId(player.teamId() == null ? null : player.teamId().value())
                    .build());
        }

        return entity;
    }

    // ---------- JPA -> domain ----------

    public GameSession toDomain(GameSessionJpaEntity entity) {
        List<ClueState> clues = entity.getClues().stream().map(this::toClue).toList();
        List<Player> players = entity.getPlayers().stream().map(this::toPlayer).toList();
        List<Team> teams = entity.getTeams().stream().map(team -> toTeam(team, entity.getPlayers())).toList();

        return GameSession.restore(
                new GameSessionId(entity.getId()),
                new OwnerId(entity.getOwnerId()),
                new QuizId(entity.getQuizId().toString()),
                entity.getLobbyCode(),
                entity.getMode(),
                entity.getState(),
                entity.getActiveTeamId() == null ? null : new TeamId(entity.getActiveTeamId()),
                entity.getCurrentBuzzedPlayerId() == null ? null : new PlayerId(entity.getCurrentBuzzedPlayerId()),
                entity.getActiveClueId() == null ? null : new ClueId(entity.getActiveClueId().toString()),
                clues,
                teams,
                players);
    }

    private ClueState toClue(ClueJpaEntity entity) {
        ClueState clue = new ClueState(
                new ClueId(entity.getId().toString()),
                entity.getCategoryName(),
                entity.getPoints(),
                entity.getQuestionText(),
                entity.getAnswerText(),
                entity.isDailyDouble());
        if (entity.isRevealed()) {
            clue.markAsRevealed();
        }
        return clue;
    }

    private Player toPlayer(PlayerJpaEntity entity) {
        return new Player(
                new PlayerId(entity.getPlayerId()),
                entity.getName(),
                entity.getTeamId() == null ? null : new TeamId(entity.getTeamId()));
    }

    private Team toTeam(TeamJpaEntity entity, List<PlayerJpaEntity> allPlayers) {
        Team team = new Team(new TeamId(entity.getId()), entity.getName(), entity.getExternalTeamId(), entity.getColour());
        team.addScore(entity.getScore());
        // Team membership is derived from the players table.
        allPlayers.stream()
                .filter(player -> entity.getId().equals(player.getTeamId()))
                .forEach(player -> team.addMember(new PlayerId(player.getPlayerId())));
        return team;
    }
}