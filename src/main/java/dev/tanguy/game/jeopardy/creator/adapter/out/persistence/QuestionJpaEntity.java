package dev.tanguy.game.jeopardy.creator.adapter.out.persistence;

import dev.tanguy.game.jeopardy.creator.domain.model.AnswerType;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "questions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private CategoryJpaEntity category;

    @Column(name = "points", nullable = false)
    private int points;

    @Column(name = "question_text", nullable = false, columnDefinition = "TEXT")
    private String questionText;

    @Column(name = "answer_text", nullable = false, columnDefinition = "TEXT")
    private String answerText;

    @Enumerated(EnumType.STRING)
    @Column(name = "answer_type", nullable = false, length = 20)
    private AnswerType answerType;

    @Column(name = "media_url", length = 2048)
    private String mediaUrl;

    @Column(name = "is_daily_double", nullable = false)
    private boolean isDailyDouble;
}
