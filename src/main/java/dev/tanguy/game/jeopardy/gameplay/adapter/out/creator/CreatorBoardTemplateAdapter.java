package dev.tanguy.game.jeopardy.gameplay.adapter.out.creator;

import dev.tanguy.game.jeopardy.common.domain.model.id.ClueId;
import dev.tanguy.game.jeopardy.common.domain.model.id.OwnerId;
import dev.tanguy.game.jeopardy.common.domain.model.id.QuizId;
import dev.tanguy.game.jeopardy.creator.api.QuizSnapshot;
import dev.tanguy.game.jeopardy.creator.api.QuizSnapshotQuery;
import dev.tanguy.game.jeopardy.gameplay.domain.model.ClueState;
import dev.tanguy.game.jeopardy.gameplay.port.out.board.LoadBoardTemplatePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CreatorBoardTemplateAdapter implements LoadBoardTemplatePort {

    private final QuizSnapshotQuery quizSnapshotQuery;

    @Override
    public List<ClueState> loadCluesForQuiz(QuizId quizId, OwnerId requesterId) {
        QuizSnapshot quiz = quizSnapshotQuery.getSnapshot(quizId, requesterId);

        return quiz.categories().stream()
                .flatMap(category -> category.questions().stream()
                        .map(q -> new ClueState(
                                ClueId.generate(),
                                category.name(),
                                q.points(),
                                q.text(),
                                q.answer(),
                                q.dailyDouble())))
                .toList();
    }
}