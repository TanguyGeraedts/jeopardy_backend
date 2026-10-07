package dev.tanguy.game.jeopardy.gameplay.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.*;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Entity
@Table(name = "game_session_players")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayerJpaEntity {

    /** Stable row id for (session, player), so re-saving the aggregate updates the row instead of re-inserting it. */
    public static UUID rowId(UUID gameSessionId, UUID playerId) {
        return UUID.nameUUIDFromBytes((gameSessionId + ":" + playerId).getBytes(StandardCharsets.UTF_8));
    }

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_session_id", nullable = false)
    private GameSessionJpaEntity gameSession;

    @Column(name = "player_id", nullable = false)
    private UUID playerId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "team_id")
    private UUID teamId;
}