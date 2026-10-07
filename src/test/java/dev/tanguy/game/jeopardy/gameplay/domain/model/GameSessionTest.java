package dev.tanguy.game.jeopardy.gameplay.domain.model;

import dev.tanguy.game.jeopardy.common.domain.model.id.ClueId;
import dev.tanguy.game.jeopardy.common.domain.model.id.GameSessionId;
import dev.tanguy.game.jeopardy.common.domain.model.id.OwnerId;
import dev.tanguy.game.jeopardy.common.domain.model.id.QuizId;
import dev.tanguy.game.jeopardy.gameplay.domain.event.GameSessionCreatedEvent;
import dev.tanguy.game.jeopardy.gameplay.domain.exception.session.EmptyBoardException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GameSessionTest {

    private static ClueState clue(String category, int points) {
        return new ClueState(ClueId.generate(), category, points, "Q" + points, "A" + points, false);
    }

    @Test
    void newSessionStartsInLobbyKeepsBoardOrderAndEmitsCreatedEvent() {
        List<ClueState> clues = List.of(clue("GEO", 100), clue("GEO", 200), clue("TECH", 100));

        GameSession session = new GameSession(
                GameSessionId.generate(), OwnerId.generate(), QuizId.generate(), clues, GameMode.TEAM);

        assertThat(session.getState()).isEqualTo(GameState.LOBBY);
        assertThat(session.getClues().values()).containsExactlyElementsOf(clues);
        assertThat(session.pullDomainEvents()).hasSize(1).first().isInstanceOf(GameSessionCreatedEvent.class);
    }

    @Test
    void rejectsAQuizWithoutQuestions() {
        QuizId quizId = QuizId.generate();

        assertThatThrownBy(() -> new GameSession(
                GameSessionId.generate(), OwnerId.generate(), quizId, List.of(), GameMode.SOLO))
                .isInstanceOf(EmptyBoardException.class);
    }
}