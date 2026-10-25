package dev.tanguy.game.jeopardy.gameplay.adapter.out.creator;

import dev.tanguy.game.jeopardy.common.domain.model.id.QuizId;
import dev.tanguy.game.jeopardy.creator.api.QuizPlayRecorder;
import dev.tanguy.game.jeopardy.gameplay.port.out.board.RecordQuizPlayPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreatorQuizPlayAdapter implements RecordQuizPlayPort {

    private final QuizPlayRecorder quizPlayRecorder;

    @Override
    public void recordPlay(QuizId quizId) {
        try {
            quizPlayRecorder.recordPlay(quizId);
        } catch (RuntimeException e) {
            // A missed count is harmless; a game that won't start over it is not.
            log.warn("Could not count a play for quiz {}", quizId, e);
        }
    }
}
