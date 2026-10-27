package dev.tanguy.game.jeopardy.gameplay.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.UUID;

/**
 * The bulk deletes are native: clues, teams, players and buzzes go with their session through the database's
 * ON DELETE CASCADE, without loading every session first.
 */
public interface GameSessionJpaRepository extends JpaRepository<GameSessionJpaEntity, UUID> {

    @Modifying
    @Query(value = "DELETE FROM game_sessions WHERE owner_id = :ownerId", nativeQuery = true)
    int deleteByOwnerId(@Param("ownerId") UUID ownerId);

    /** created_at is set by the database on insert (V3); the entity doesn't map it. */
    @Modifying
    @Query(value = "DELETE FROM game_sessions WHERE created_at < :cutoff", nativeQuery = true)
    int deleteCreatedBefore(@Param("cutoff") Instant cutoff);
}
