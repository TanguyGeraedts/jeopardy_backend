package dev.tanguy.game.jeopardy.creator.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QuizJpaRepository extends JpaRepository<QuizJpaEntity, UUID> {
    List<QuizJpaEntity> findByOwnerId(UUID ownerId);
}
