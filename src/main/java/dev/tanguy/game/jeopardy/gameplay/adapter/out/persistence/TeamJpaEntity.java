package dev.tanguy.game.jeopardy.gameplay.adapter.out.persistence;

import dev.tanguy.game.jeopardy.gameplay.domain.model.BuzzerSound;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "game_session_teams")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_session_id", nullable = false)
    private GameSessionJpaEntity gameSession;

    @Column(name = "name", nullable = false)
    private String name;

    /** The lobby's team order (scoreboard and turn rotation). */
    @Column(name = "position", nullable = false)
    private int position;

    @Column(name = "external_team_id")
    private String externalTeamId;

    @Column(name = "colour", length = 50)
    private String colour;

    @Column(name = "score", nullable = false)
    private int score;

    @Column(name = "locked_out", nullable = false)
    private boolean lockedOut;

    /** Null = automatic (by team position). */
    @Enumerated(EnumType.STRING)
    @Column(name = "buzzer_sound", length = 20)
    private BuzzerSound buzzerSound;
}