package dev.tanguy.game.jeopardy.gameplay.domain.model;

import dev.tanguy.game.jeopardy.common.domain.exception.DomainConflictException;
import dev.tanguy.game.jeopardy.common.domain.model.id.ClueId;
import dev.tanguy.game.jeopardy.common.domain.model.id.GameSessionId;
import dev.tanguy.game.jeopardy.common.domain.model.id.OwnerId;
import dev.tanguy.game.jeopardy.common.domain.model.id.PlayerId;
import dev.tanguy.game.jeopardy.common.domain.model.id.QuizId;
import dev.tanguy.game.jeopardy.common.domain.model.id.TeamId;
import dev.tanguy.game.jeopardy.common.events.DomainEvent;
import dev.tanguy.game.jeopardy.gameplay.domain.event.ClueSelectedEvent;
import dev.tanguy.game.jeopardy.gameplay.domain.event.GameSessionCreatedEvent;
import dev.tanguy.game.jeopardy.gameplay.domain.event.GameStateChangedEvent;
import dev.tanguy.game.jeopardy.gameplay.domain.event.AnswerEvaluatedEvent;
import dev.tanguy.game.jeopardy.gameplay.domain.event.BuzzerPressedEvent;
import dev.tanguy.game.jeopardy.gameplay.domain.event.PlayerJoinedEvent;
import dev.tanguy.game.jeopardy.gameplay.domain.event.TeamCreatedEvent;
import dev.tanguy.game.jeopardy.gameplay.domain.exception.clue.ClueAlreadyRevealedException;
import dev.tanguy.game.jeopardy.gameplay.domain.exception.clue.ClueNotFoundException;
import dev.tanguy.game.jeopardy.gameplay.domain.exception.player.PlayerNotFoundException;
import dev.tanguy.game.jeopardy.gameplay.domain.exception.player.TeamAssignmentRequiredException;
import dev.tanguy.game.jeopardy.gameplay.domain.exception.player.UnexpectedTeamAssignmentException;
import dev.tanguy.game.jeopardy.gameplay.domain.exception.session.EmptyBoardException;
import dev.tanguy.game.jeopardy.gameplay.domain.exception.session.IllegalGameStateTransitionException;
import dev.tanguy.game.jeopardy.gameplay.domain.exception.session.LobbyCodeAlreadyAssignedException;
import dev.tanguy.game.jeopardy.gameplay.domain.exception.team.TeamNotFoundException;
import lombok.Getter;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Getter
public class GameSession {

    private final GameSessionId id;
    /** The creator/admin who started this session. Never a player. */
    private final OwnerId ownerId;
    /** Which quiz the board was copied from. Reference only: the clues below are a snapshot. */
    private final QuizId quizId;
    private final GameMode mode;
    private final Map<PlayerId, Player> players = new HashMap<>();
    /** Insertion order = the lobby's team order: the scoreboard and the turn rotation follow it. */
    private final Map<TeamId, Team> teams = new LinkedHashMap<>();
    /** Insertion order = board order (category by category, lowest points first). */
    private final Map<ClueId, ClueState> clues = new LinkedHashMap<>();
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    /** Room code issued by the lobby microservice. Null until the lobby has been created. */
    private String lobbyCode;

    private GameState state = GameState.LOBBY;
    private TeamId activeTeamId;
    /** The player answering right now (head of the buzz queue). Null unless ANSWER_EVALUATION. */
    private PlayerId currentBuzzedPlayerId;
    private ClueId activeClueId;
    /** When the host opened the buzzers on the open clue. Null while it is being read (and on the board). */
    private Instant buzzersOpenedAt;
    /**
     * Every buzz on the open clue, in arrival order: at most one player per team. Locked-out teams stay in the list
     * (screens show them crossed out, and the gaps stay measured to the very first buzz); the queue skips them.
     */
    private final List<Buzz> buzzes = new ArrayList<>();

    private TurnRules turnRules = TurnRules.OFF;
    /** Whose turn it is: that team gets the head start on the next clue. Null while turns were never on. */
    private TeamId turnTeamId;
    /**
     * The team with the head start on the open clue: fixed when the clue is picked (the host can still move it while
     * the clue is read). Null without turns. If the host moves the turn after that, it's for the next clue.
     */
    private TeamId clueTurnTeamId;

    /** Starts a brand new session. Emits GameSessionCreatedEvent. */
    public GameSession(GameSessionId id, OwnerId ownerId, QuizId quizId, List<ClueState> initialClues, GameMode mode) {
        this(id, ownerId, quizId, mode);
        if (initialClues == null || initialClues.isEmpty()) {
            throw new EmptyBoardException(quizId);
        }
        initialClues.forEach(clue -> this.clues.put(clue.getId(), clue));
        this.domainEvents.add(new GameSessionCreatedEvent(this.id));
    }

    private GameSession(GameSessionId id, OwnerId ownerId, QuizId quizId, GameMode mode) {
        this.id = Objects.requireNonNull(id, "GameSessionId cannot be null");
        this.ownerId = Objects.requireNonNull(ownerId, "OwnerId cannot be null");
        this.quizId = Objects.requireNonNull(quizId, "QuizId cannot be null");
        this.mode = Objects.requireNonNull(mode, "GameMode cannot be null");
    }

    /** Rebuilds a session from storage. No validation of transitions and no events. */
    public static GameSession restore(
            GameSessionId id, OwnerId ownerId, QuizId quizId, String lobbyCode, GameMode mode,
            GameState state, TeamId activeTeamId, PlayerId currentBuzzedPlayerId, ClueId activeClueId,
            Instant buzzersOpenedAt, List<Buzz> buzzes,
            TurnRules turnRules, TeamId turnTeamId, TeamId clueTurnTeamId,
            List<ClueState> clues, List<Team> teams, List<Player> players
    ) {
        GameSession session = new GameSession(id, ownerId, quizId, mode);
        session.lobbyCode = lobbyCode;
        session.state = state;
        session.activeTeamId = activeTeamId;
        session.currentBuzzedPlayerId = currentBuzzedPlayerId;
        session.activeClueId = activeClueId;
        session.buzzersOpenedAt = buzzersOpenedAt;
        session.buzzes.addAll(buzzes);
        session.turnRules = turnRules;
        session.turnTeamId = turnTeamId;
        session.clueTurnTeamId = clueTurnTeamId;
        clues.forEach(clue -> session.clues.put(clue.getId(), clue));
        teams.forEach(team -> session.teams.put(team.getId(), team));
        players.forEach(player -> session.players.put(player.id(), player));
        return session;
    }

    /** Called once, when the lobby microservice has created the room for this session. */
    public void assignLobbyCode(String lobbyCode) {
        if (lobbyCode == null || lobbyCode.isBlank()) {
            throw new IllegalArgumentException("Lobby code cannot be blank");
        }
        if (this.lobbyCode != null) {
            throw new LobbyCodeAlreadyAssignedException(this.id);
        }
        this.lobbyCode = lobbyCode.strip();
    }

    public Team createTeam(TeamId teamId, String teamName) {
        return createTeam(teamId, teamName, null, null);
    }

    public Team createTeam(TeamId teamId, String teamName, String externalTeamId, String colour) {
        if (state != GameState.LOBBY) {
            throw new IllegalGameStateTransitionException(state, "createTeam");
        }
        Team team = new Team(teamId, teamName, externalTeamId, colour);
        teams.put(teamId, team);
        this.domainEvents.add(new TeamCreatedEvent(this.id, teamId, teamName));
        return team;
    }

    public void addPlayer(PlayerId playerId, String name, TeamId teamId) {
        if (players.containsKey(playerId)) {
            return;
        }

        if (state != GameState.LOBBY) {
            throw new IllegalGameStateTransitionException(state, "addPlayer");
        }

        if (mode == GameMode.TEAM && teamId == null) {
            throw new TeamAssignmentRequiredException();
        }
        if (mode == GameMode.SOLO && teamId != null) {
            throw new UnexpectedTeamAssignmentException(teamId);
        }

        // Solo Mode auto-creation of a Team if no teamId provided
        TeamId assignedTeamId = teamId;
        if (assignedTeamId == null) {
            assignedTeamId = TeamId.generate();
            Team soloTeam = new Team(assignedTeamId, name);
            teams.put(assignedTeamId, soloTeam);
            this.domainEvents.add(new TeamCreatedEvent(this.id, assignedTeamId, name));
        }

        Team targetTeam = teams.get(assignedTeamId);
        if (targetTeam == null) {
            throw new TeamNotFoundException(assignedTeamId);
        }

        Player player = new Player(playerId, name, assignedTeamId);
        players.put(playerId, player);
        targetTeam.addMember(playerId);

        this.domainEvents.add(new PlayerJoinedEvent(this.id, playerId, assignedTeamId, name));
    }

    public Optional<Team> findTeamByExternalId(String externalTeamId) {
        if (externalTeamId == null) {
            return Optional.empty();
        }
        return teams.values().stream()
                .filter(team -> externalTeamId.equals(team.getExternalTeamId()))
                .findFirst();
    }

    public void startGame() {
        if (players.isEmpty() || teams.isEmpty()) {
            throw new DomainConflictException("Cannot start game without players and teams") {};
        }
        this.activeTeamId = teams.keySet().iterator().next();
        if (turnRules.enabled()) {
            this.turnTeamId = activeTeamId;
        }
        this.state = GameState.BOARD_SELECTION;

        this.domainEvents.add(new GameStateChangedEvent(this.id, this.state));
    }

    // ---------- turns ----------

    /** Set when the game is created; the host can change it any time. Switching on starts with the first team. */
    public void changeTurnRules(TurnRules rules) {
        this.turnRules = Objects.requireNonNull(rules, "rules cannot be null");
        if (rules.enabled() && turnTeamId == null && state != GameState.LOBBY && !teams.isEmpty()) {
            this.turnTeamId = teams.keySet().iterator().next();
        }
    }

    /**
     * The host hands the turn to a team (e.g. one that's far behind); the rotation continues from there.
     * Before the buzzers open it counts for the clue being read; after that, from the next clue.
     */
    public void giveTurn(TeamId teamId) {
        requireTeam(teamId);
        this.turnTeamId = teamId;
        if (state == GameState.CLUE_READING && turnRules.enabled()) {
            this.clueTurnTeamId = teamId;
        }
    }

    /** The turn goes to the next team in the rotation. */
    public void skipTurn() {
        if (teams.isEmpty()) {
            return;
        }
        giveTurn(teamAfter(turnTeamId));
    }

    /** The next team in lobby order, wrapping around. Null or unknown = the first team. */
    private TeamId teamAfter(TeamId current) {
        List<TeamId> order = new ArrayList<>(teams.keySet());
        int index = current == null ? -1 : order.indexOf(current);
        return order.get((index + 1) % order.size());
    }

    /** When anyone may buzz on the open clue: the end of the turn team's head start. Null = no head start. */
    public Instant stealOpensAt() {
        if (clueTurnTeamId == null || buzzersOpenedAt == null || isOpenClueDailyDouble()) {
            return null;
        }
        // The turn team answered wrongly: no need to wait out the rest of its head start.
        Team turnTeam = teams.get(clueTurnTeamId);
        if (turnTeam != null && turnTeam.isLockedOut()) {
            return buzzersOpenedAt;
        }
        return buzzersOpenedAt.plusSeconds(turnRules.headStartSeconds());
    }

    private boolean isOpenClueDailyDouble() {
        ClueState clue = activeClueId == null ? null : clues.get(activeClueId);
        return clue != null && clue.isDailyDouble();
    }

    /** The host opens a clue. Buzzers stay closed while it is being read (CLUE_READING). */
    public void selectClue(ClueId clueId) {
        if (state != GameState.BOARD_SELECTION) {
            throw new IllegalGameStateTransitionException(state, "selectClue");
        }

        ClueState clue = clues.get(clueId);
        if (clue == null) {
            throw new ClueNotFoundException(clueId);
        }
        if (clue.isRevealed()) {
            throw new ClueAlreadyRevealedException(clueId);
        }

        clue.markAsRevealed();
        this.activeClueId = clueId;
        this.currentBuzzedPlayerId = null;
        this.buzzersOpenedAt = null;
        this.buzzes.clear();
        teams.values().forEach(team -> team.setLockedOut(false));
        this.clueTurnTeamId = turnRules.enabled() ? turnTeamId : null;
        this.state = GameState.CLUE_READING;

        this.domainEvents.add(new ClueSelectedEvent(this.id, clueId));
        this.domainEvents.add(new GameStateChangedEvent(this.id, this.state));
    }

    /**
     * The host finished reading. With turns, the turn team may buzz from `at` and everyone else after its head start.
     * Without turns, the buzzers open `randomDelay` later, so nobody can time a buzz off the host's click.
     */
    public void openBuzzers(Instant at, Duration randomDelay) {
        if (state != GameState.CLUE_READING) {
            throw new IllegalGameStateTransitionException(state, "openBuzzers");
        }
        Objects.requireNonNull(at, "at cannot be null");
        this.buzzersOpenedAt = clueTurnTeamId != null ? at : at.plus(randomDelay);
        this.state = GameState.BUZZER_OPEN;
        this.domainEvents.add(new GameStateChangedEvent(this.id, this.state));
    }

    /**
     * Records a buzz in arrival order. Buzzes keep queueing while someone answers, so the next team is up
     * right away after a wrong answer. `at` is when the server received the buzz: it decides the order, not who got
     * the session lock first.
     * TOO_EARLY: the clue is still being read, the buzzers haven't opened yet, or it's another team's head start
     * (the caller locks the player out for a moment, so mashing doesn't pay). IGNORED: it doesn't count, no penalty.
     */
    public BuzzOutcome registerBuzz(PlayerId playerId, Instant at) {
        Player player = players.get(playerId);
        if (player == null) {
            throw new PlayerNotFoundException(playerId);
        }
        if (state == GameState.CLUE_READING) {
            return BuzzOutcome.TOO_EARLY;
        }
        if (state != GameState.BUZZER_OPEN && state != GameState.ANSWER_EVALUATION) {
            return BuzzOutcome.IGNORED;
        }
        if (buzzersOpenedAt != null && at.isBefore(buzzersOpenedAt)) {
            return BuzzOutcome.TOO_EARLY;
        }

        TeamId teamId = player.getTeamId().orElseThrow(() -> new TeamNotFoundException(null));
        if (!canBuzzOnOpenClue(teamId)) {
            return BuzzOutcome.IGNORED;
        }
        Instant stealAt = stealOpensAt();
        if (stealAt != null && !teamId.equals(clueTurnTeamId) && at.isBefore(stealAt)) {
            return BuzzOutcome.TOO_EARLY;
        }

        int index = 0;
        while (index < buzzes.size() && !buzzes.get(index).at().isAfter(at)) {
            index++;
        }
        buzzes.add(index, new Buzz(playerId, at));
        this.domainEvents.add(new BuzzerPressedEvent(this.id, playerId));

        // Usually unchanged; differs only when this buzz arrived a few ms before the one that got the lock first.
        this.currentBuzzedPlayerId = firstInLine();
        if (state == GameState.BUZZER_OPEN) {
            this.state = GameState.ANSWER_EVALUATION;
            this.domainEvents.add(new GameStateChangedEvent(this.id, this.state));
        }
        return BuzzOutcome.ACCEPTED;
    }

    /** The earliest buzz from a team that isn't locked out, or null. */
    private PlayerId firstInLine() {
        return buzzes.stream()
                .map(Buzz::playerId)
                .filter(playerId -> {
                    Team team = teams.get(teamOf(playerId));
                    return team != null && !team.isLockedOut();
                })
                .findFirst()
                .orElse(null);
    }

    /** Whether a buzz from this team would count now (ignoring the game state). */
    public boolean canBuzz(TeamId teamId) {
        Team team = teams.get(teamId);
        if (team == null || team.isLockedOut()) {
            return false;
        }
        return buzzes.stream().noneMatch(buzz -> teamId.equals(teamOf(buzz.playerId())));
    }

    /** canBuzz, plus: with turns on, a daily double belongs to the turn team only (no stealing). Ignores the clock. */
    public boolean canBuzzOnOpenClue(TeamId teamId) {
        if (!canBuzz(teamId)) {
            return false;
        }
        return clueTurnTeamId == null || !isOpenClueDailyDouble() || clueTurnTeamId.equals(teamId);
    }

    /**
     * The host judges the answering player. Correct: +points, that team picks next, back to the board.
     * Wrong: -points, the team is locked out for this clue and the next team in the queue is up
     * (or buzzers stay open if nobody else has buzzed yet).
     */
    public void evaluateAnswer(boolean isCorrect) {
        if (state != GameState.ANSWER_EVALUATION) {
            throw new IllegalGameStateTransitionException(state, "evaluateAnswer");
        }

        ClueState clue = clues.get(activeClueId);
        if (clue == null) {
            throw new ClueNotFoundException(activeClueId);
        }

        PlayerId answeringPlayerId = currentBuzzedPlayerId;
        TeamId teamId = teamOf(answeringPlayerId);
        Team team = teamId == null ? null : teams.get(teamId);
        if (team == null) {
            throw new TeamNotFoundException(teamId);
        }

        if (isCorrect) {
            team.addScore(clue.getValue());
            this.activeTeamId = team.getId();
            resetToBoardSelection();
        } else {
            team.subtractScore(clue.getValue());
            team.setLockedOut(true);
            this.currentBuzzedPlayerId = firstInLine();
            this.state = currentBuzzedPlayerId == null ? GameState.BUZZER_OPEN : GameState.ANSWER_EVALUATION;
        }

        this.domainEvents.add(new AnswerEvaluatedEvent(this.id, answeringPlayerId, teamId, isCorrect, clue.getValue()));
        this.domainEvents.add(new GameStateChangedEvent(this.id, this.state));
    }

    /** The host ends the open clue without (more) points, e.g. nobody knows it or time is up. */
    public void closeClue() {
        if (state != GameState.CLUE_READING && state != GameState.BUZZER_OPEN && state != GameState.ANSWER_EVALUATION) {
            throw new IllegalGameStateTransitionException(state, "closeClue");
        }
        resetToBoardSelection();
        this.domainEvents.add(new GameStateChangedEvent(this.id, this.state));
    }

    /** The host stops early (e.g. out of time): the final scores are what they are now. Unplayed clues stay unplayed. */
    public void endGame() {
        if (state == GameState.LOBBY || state == GameState.GAME_OVER) {
            throw new IllegalGameStateTransitionException(state, "endGame");
        }
        clearOpenClue();
        this.state = GameState.GAME_OVER;
        this.domainEvents.add(new GameStateChangedEvent(this.id, this.state));
    }

    /** Host correction, any time after the start. */
    public void adjustScore(TeamId teamId, int delta) {
        requireTeam(teamId).addScore(delta);
    }

    /** Host correction, any time after the start. */
    public void setScore(TeamId teamId, int score) {
        requireTeam(teamId).setScore(score);
    }

    /** The sound the board plays when this team buzzes. Null = automatic. Allowed any time, even mid-clue. */
    public void setTeamBuzzerSound(TeamId teamId, BuzzerSound sound) {
        requireTeam(teamId).setBuzzerSound(sound);
    }

    public Optional<Player> findPlayer(PlayerId playerId) {
        return Optional.ofNullable(players.get(playerId));
    }

    private Team requireTeam(TeamId teamId) {
        Team team = teams.get(teamId);
        if (team == null) {
            throw new TeamNotFoundException(teamId);
        }
        return team;
    }

    private TeamId teamOf(PlayerId playerId) {
        Player player = playerId == null ? null : players.get(playerId);
        return player == null ? null : player.teamId();
    }

    private void resetToBoardSelection() {
        // Turns rotate after every clue, whatever happened. Unless the host moved the turn during the clue:
        // then that team is up next.
        if (turnRules.enabled() && clueTurnTeamId != null && clueTurnTeamId.equals(turnTeamId)) {
            this.turnTeamId = teamAfter(turnTeamId);
        }
        clearOpenClue();
        boolean allRevealed = clues.values().stream().allMatch(ClueState::isRevealed);
        this.state = allRevealed ? GameState.GAME_OVER : GameState.BOARD_SELECTION;
    }

    public List<DomainEvent> pullDomainEvents() {
        var copy = List.copyOf(domainEvents);
        domainEvents.clear();
        return copy;
    }

    private void clearOpenClue() {
        this.activeClueId = null;
        this.currentBuzzedPlayerId = null;
        this.buzzersOpenedAt = null;
        this.clueTurnTeamId = null;
        this.buzzes.clear();
        teams.values().forEach(team -> team.setLockedOut(false));
    }

    public List<Buzz> getBuzzes() { return Collections.unmodifiableList(buzzes); }
    public Collection<Player> getPlayers() { return Collections.unmodifiableCollection(players.values()); }
    public Collection<Team> getTeams() { return Collections.unmodifiableCollection(teams.values()); }
}