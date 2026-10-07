package dev.tanguy.game.jeopardy.gameplay.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "game_session_clues")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClueJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_session_id", nullable = false)
    private GameSessionJpaEntity gameSession;

    /** Board position: category by category, lowest points first. */
    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Column(name = "category_name", nullable = false)
    private String categoryName;

    @Column(name = "points", nullable = false)
    private int points;

    @Column(name = "question_text", nullable = false, columnDefinition = "TEXT")
    private String questionText;

    @Column(name = "answer_text", nullable = false, columnDefinition = "TEXT")
    private String answerText;

    @Column(name = "is_daily_double", nullable = false)
    private boolean dailyDouble;

    @Column(name = "is_revealed", nullable = false)
    private boolean revealed;
}