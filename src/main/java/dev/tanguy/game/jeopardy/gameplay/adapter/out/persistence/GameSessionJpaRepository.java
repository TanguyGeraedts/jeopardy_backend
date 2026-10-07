package dev.tanguy.game.jeopardy.gameplay.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface GameSessionJpaRepository extends JpaRepository<GameSessionJpaEntity, UUID> {
}