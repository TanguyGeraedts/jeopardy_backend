package dev.tanguy.game.jeopardy.creator.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.List;
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
