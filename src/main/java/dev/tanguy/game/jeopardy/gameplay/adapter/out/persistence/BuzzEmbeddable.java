package dev.tanguy.game.jeopardy.gameplay.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/** One row of game_session_buzzes. */
@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BuzzEmbeddable {

    @Column(name = "player_id", nullable = false)
    private UUID playerId;

    @Column(name = "buzzed_at", nullable = false)
    private Instant buzzedAt;
}
