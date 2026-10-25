package dev.tanguy.game.jeopardy.creator.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import dev.tanguy.game.jeopardy.creator.domain.model.QuizTopic;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "quizzes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "published", nullable = false)
    private boolean published;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "author_name", length = 60)
    private String authorName;

    /** Read-only here: changed only by QuizJpaRepository.incrementTimesPlayed, so a save never overwrites a play. */
    @Column(name = "times_played", insertable = false, updatable = false)
    private int timesPlayed;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "quiz_topics", joinColumns = @JoinColumn(name = "quiz_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "topic", nullable = false, length = 30)
    @BatchSize(size = 50)
    @Builder.Default
    private Set<QuizTopic> topics = new HashSet<>();

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("sortOrder ASC")
    @BatchSize(size = 50)
    @Builder.Default
    private List<CategoryJpaEntity> categories = new ArrayList<>();

    public void addCategory(CategoryJpaEntity category) {
        categories.add(category);
        category.setQuiz(this);
    }

    public void removeCategory(CategoryJpaEntity category) {
        categories.remove(category);
        category.setQuiz(null);
    }
}
