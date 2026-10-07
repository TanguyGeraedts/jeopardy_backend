package dev.tanguy.game.jeopardy.gameplay.adapter.out.persistence;

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

    @Column(name = "external_team_id")
    private String externalTeamId;

    @Column(name = "colour", length = 50)
    private String colour;

    @Column(name = "score", nullable = false)
    private int score;
}