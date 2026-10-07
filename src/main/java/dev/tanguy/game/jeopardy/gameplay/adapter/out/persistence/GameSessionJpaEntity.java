package dev.tanguy.game.jeopardy.gameplay.adapter.out.persistence;

import dev.tanguy.game.jeopardy.gameplay.domain.model.GameMode;
import dev.tanguy.game.jeopardy.gameplay.domain.model.GameState;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "game_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameSessionJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "owner_id", nullable = false, updatable = false)
    private UUID ownerId;

    @Column(name = "quiz_id", nullable = false, updatable = false)
    private UUID quizId;

    /** Room code issued by the lobby microservice; null until the lobby exists. */
    @Column(name = "lobby_code", length = 64)
    private String lobbyCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "mode", nullable = false, length = 10, updatable = false)
    private GameMode mode;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false, length = 30)
    private GameState state;

    @Column(name = "active_team_id")
    private UUID activeTeamId;

    @Column(name = "current_buzzed_player_id")
    private UUID currentBuzzedPlayerId;

    @Column(name = "active_clue_id")
    private UUID activeClueId;

    @OneToMany(mappedBy = "gameSession", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("sortOrder ASC")
    @BatchSize(size = 50)
    @Builder.Default
    private List<ClueJpaEntity> clues = new ArrayList<>();

    @OneToMany(mappedBy = "gameSession", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @BatchSize(size = 50)
    @Builder.Default
    private List<TeamJpaEntity> teams = new ArrayList<>();

    @OneToMany(mappedBy = "gameSession", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @BatchSize(size = 50)
    @Builder.Default
    private List<PlayerJpaEntity> players = new ArrayList<>();

    public void addClue(ClueJpaEntity clue) {
        clues.add(clue);
        clue.setGameSession(this);
    }

    public void addTeam(TeamJpaEntity team) {
        teams.add(team);
        team.setGameSession(this);
    }

    public void addPlayer(PlayerJpaEntity player) {
        players.add(player);
        player.setGameSession(this);
    }
}