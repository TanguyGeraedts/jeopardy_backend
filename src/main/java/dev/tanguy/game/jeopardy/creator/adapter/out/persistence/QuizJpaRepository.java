package dev.tanguy.game.jeopardy.creator.adapter.out.persistence;

import dev.tanguy.game.jeopardy.creator.domain.model.QuizTopic;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QuizJpaRepository extends JpaRepository<QuizJpaEntity, UUID> {
    List<QuizJpaEntity> findByOwnerId(UUID ownerId);

    /**
     * Published quizzes whose name or author matches `pattern` (a lower-case LIKE pattern; "%" = everything),
     * optionally with one topic. anyTopic = true ignores `topic` (Postgres can't type a null enum parameter).
     */
    @Query("""
            select q from QuizJpaEntity q
            where q.published = true
              and (lower(q.name) like :pattern or lower(coalesce(q.authorName, '')) like :pattern)
              and (:anyTopic = true or :topic member of q.topics)
            """)
    List<QuizJpaEntity> searchPublished(
            @Param("pattern") String pattern,
            @Param("anyTopic") boolean anyTopic,
            @Param("topic") QuizTopic topic,
            Pageable pageable);

    /** Native on purpose: times_played isn't updatable through the entity, so saves never overwrite a play. */
    @Modifying
    @Query(value = "UPDATE quizzes SET times_played = times_played + 1 WHERE id = :id", nativeQuery = true)
    int incrementTimesPlayed(@Param("id") UUID id);
}
