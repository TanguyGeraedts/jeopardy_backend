package dev.tanguy.game.jeopardy.gameplay.domain.model;

import dev.tanguy.game.jeopardy.common.domain.model.id.ClueId;
import dev.tanguy.game.jeopardy.common.domain.model.id.GameSessionId;
import dev.tanguy.game.jeopardy.common.domain.model.id.OwnerId;
import dev.tanguy.game.jeopardy.common.domain.model.id.PlayerId;
import dev.tanguy.game.jeopardy.common.domain.model.id.QuizId;
import dev.tanguy.game.jeopardy.common.domain.model.id.TeamId;
import dev.tanguy.game.jeopardy.gameplay.domain.event.GameSessionCreatedEvent;
import dev.tanguy.game.jeopardy.gameplay.domain.exception.session.EmptyBoardException;
import dev.tanguy.game.jeopardy.gameplay.domain.exception.team.TeamNotFoundException;
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

    // ---------- buzzers ----------

    private final TeamId red = TeamId.generate();
    private final TeamId blue = TeamId.generate();
    private final PlayerId ana = PlayerId.generate();
    private final PlayerId ben = PlayerId.generate();
    private final PlayerId cas = PlayerId.generate();
    private final ClueState first = clue("GEO", 100);
    private final ClueState second = clue("GEO", 200);

    /** Two teams (Ana + Ben in red, Cas in blue), game started, first clue open and buzzers open. */
    private GameSession openClue() {
        GameSession session = new GameSession(
                GameSessionId.generate(), OwnerId.generate(), QuizId.generate(), List.of(first, second), GameMode.TEAM);
        session.createTeam(red, "Red");
        session.createTeam(blue, "Blue");
        session.addPlayer(ana, "Ana", red);
        session.addPlayer(ben, "Ben", red);
        session.addPlayer(cas, "Cas", blue);
        session.startGame();
        session.selectClue(first.getId());
        session.openBuzzers();
        return session;
    }

    private static int score(GameSession session, TeamId teamId) {
        return session.getTeams().stream().filter(t -> t.getId().equals(teamId)).findFirst().orElseThrow().getScore();
    }

    @Test
    void buzzesBeforeTheHostOpensThemAreIgnored() {
        GameSession session = new GameSession(
                GameSessionId.generate(), OwnerId.generate(), QuizId.generate(), List.of(first), GameMode.SOLO);
        session.addPlayer(ana, "Ana", null);
        session.startGame();
        session.selectClue(first.getId());

        assertThat(session.registerBuzz(ana)).isFalse();
        assertThat(session.getState()).isEqualTo(GameState.CLUE_READING);
        assertThat(session.getBuzzQueue()).isEmpty();
    }

    @Test
    void firstBuzzAnswersAndLaterBuzzesQueueOnePerTeam() {
        GameSession session = openClue();

        assertThat(session.registerBuzz(cas)).isTrue();
        assertThat(session.registerBuzz(ana)).isTrue();
        assertThat(session.registerBuzz(ben)).isFalse(); // red already in line
        assertThat(session.registerBuzz(cas)).isFalse(); // mashing

        assertThat(session.getState()).isEqualTo(GameState.ANSWER_EVALUATION);
        assertThat(session.getCurrentBuzzedPlayerId()).isEqualTo(cas);
        assertThat(session.getBuzzQueue()).containsExactly(cas, ana);
    }

    @Test
    void wrongAnswerLocksTheTeamOutAndTheNextInLineIsUp() {
        GameSession session = openClue();
        session.registerBuzz(cas);
        session.registerBuzz(ana);

        session.evaluateAnswer(false);

        assertThat(score(session, blue)).isEqualTo(-100);
        assertThat(session.getCurrentBuzzedPlayerId()).isEqualTo(ana);
        assertThat(session.getState()).isEqualTo(GameState.ANSWER_EVALUATION);
        assertThat(session.canBuzz(blue)).isFalse();
        assertThat(session.registerBuzz(cas)).isFalse();
    }

    @Test
    void wrongAnswerWithNobodyInLineReopensBuzzers() {
        GameSession session = openClue();
        session.registerBuzz(cas);

        session.evaluateAnswer(false);

        assertThat(session.getState()).isEqualTo(GameState.BUZZER_OPEN);
        assertThat(session.getCurrentBuzzedPlayerId()).isNull();
        assertThat(session.registerBuzz(ben)).isTrue();
        assertThat(session.getCurrentBuzzedPlayerId()).isEqualTo(ben);
    }

    @Test
    void correctAnswerScoresAndTheNextClueStartsClean() {
        GameSession session = openClue();
        session.registerBuzz(cas);
        session.evaluateAnswer(false);
        session.registerBuzz(ana);

        session.evaluateAnswer(true);

        assertThat(score(session, red)).isEqualTo(100);
        assertThat(session.getActiveTeamId()).isEqualTo(red);
        assertThat(session.getState()).isEqualTo(GameState.BOARD_SELECTION);
        assertThat(session.getBuzzQueue()).isEmpty();

        session.selectClue(second.getId());
        session.openBuzzers();
        assertThat(session.canBuzz(blue)).isTrue(); // lockout was for the previous clue only
    }

    @Test
    void closingAClueGivesNoPointsAndEndsTheGameAfterTheLastClue() {
        GameSession session = openClue();
        session.registerBuzz(cas);
        session.closeClue();
        assertThat(session.getState()).isEqualTo(GameState.BOARD_SELECTION);
        assertThat(score(session, blue)).isZero();

        session.selectClue(second.getId());
        session.closeClue();
        assertThat(session.getState()).isEqualTo(GameState.GAME_OVER);
    }

    @Test
    void hostPicksABuzzerSoundPerTeam() {
        GameSession session = openClue();

        session.setTeamBuzzerSound(red, BuzzerSound.GONG);
        assertThat(session.getTeams()).filteredOn(t -> t.getId().equals(red)).first()
                .extracting(Team::getBuzzerSound).isEqualTo(BuzzerSound.GONG);

        session.setTeamBuzzerSound(red, null); // back to automatic
        assertThat(session.getTeams()).filteredOn(t -> t.getId().equals(red)).first()
                .extracting(Team::getBuzzerSound).isNull();

        assertThatThrownBy(() -> session.setTeamBuzzerSound(TeamId.generate(), BuzzerSound.BELL))
                .isInstanceOf(TeamNotFoundException.class);
    }

    @Test
    void hostCanCorrectScores() {
        GameSession session = openClue();

        session.adjustScore(red, 500);
        session.adjustScore(red, -100);
        session.setScore(blue, 42);

        assertThat(score(session, red)).isEqualTo(400);
        assertThat(score(session, blue)).isEqualTo(42);
    }
}