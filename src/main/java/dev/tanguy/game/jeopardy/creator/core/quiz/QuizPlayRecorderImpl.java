package dev.tanguy.game.jeopardy.creator.core.quiz;

import dev.tanguy.game.jeopardy.common.domain.model.id.QuizId;
import dev.tanguy.game.jeopardy.creator.api.QuizPlayRecorder;
import dev.tanguy.game.jeopardy.creator.port.out.QuizRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QuizPlayRecorderImpl implements QuizPlayRecorder {

    private final QuizRepositoryPort quizRepositoryPort;

    @Override
    public void recordPlay(QuizId quizId) {
        quizRepositoryPort.incrementTimesPlayed(quizId);
    }
}
