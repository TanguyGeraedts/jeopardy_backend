package dev.tanguy.game.jeopardy.creator.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private QuizJpaEntity quiz;

    @Column(name = "name", nullable = false)
    private String name;

    /** Position of the category inside its quiz, so the board order survives a round trip. */
    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("points ASC")
    @BatchSize(size = 50)
    @Builder.Default
    private List<QuestionJpaEntity> questions = new ArrayList<>();

    public void addQuestion(QuestionJpaEntity question) {
        questions.add(question);
        question.setCategory(this);
    }

    public void removeQuestion(QuestionJpaEntity question) {
        questions.remove(question);
        question.setCategory(null);
    }
}
