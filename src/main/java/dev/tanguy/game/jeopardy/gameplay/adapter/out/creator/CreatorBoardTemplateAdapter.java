package dev.tanguy.game.jeopardy.gameplay.adapter.out.creator;

import dev.tanguy.game.jeopardy.common.domain.model.id.ClueId;
import dev.tanguy.game.jeopardy.common.domain.model.id.OwnerId;
import dev.tanguy.game.jeopardy.common.domain.model.id.QuizId;
import dev.tanguy.game.jeopardy.creator.api.QuizSnapshot;
import dev.tanguy.game.jeopardy.creator.api.QuizSnapshotQuery;
import dev.tanguy.game.jeopardy.gameplay.domain.model.ClueMediaType;
import dev.tanguy.game.jeopardy.gameplay.domain.model.ClueState;
import dev.tanguy.game.jeopardy.gameplay.port.out.board.LoadBoardTemplatePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
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
                                q.dailyDouble(),
                                toClueMediaType(q.mediaType()),
                                q.mediaUrl())))
                .toList();
    }

    /**
     * The creator's media type (a name, so the modules stay apart) as gameplay's. Spelled out on purpose: a type
     * the board can't show yet becomes a text-only clue instead of making the whole game fail to start.
     */
    private static ClueMediaType toClueMediaType(String creatorMediaType) {
        if (creatorMediaType == null) {
            return null;
        }
        return switch (creatorMediaType) {
            case "IMAGE" -> ClueMediaType.IMAGE;
            case "VIDEO" -> ClueMediaType.VIDEO;
            case "AUDIO" -> ClueMediaType.AUDIO;
            default -> {
                log.warn("Media type {} has no board equivalent; the clue is shown as text only", creatorMediaType);
                yield null;
            }
        };
    }
}