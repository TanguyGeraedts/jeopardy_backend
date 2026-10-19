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
    private final Map<TeamId, Team> teams = new HashMap<>();
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
    /** Who buzzed on the open clue, in arrival order: at most one player per team. Locked-out teams are removed. */
    private final List<PlayerId> buzzQueue = new ArrayList<>();

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
            List<PlayerId> buzzQueue,
            List<ClueState> clues, List<Team> teams, List<Player> players
    ) {
        GameSession session = new GameSession(id, ownerId, quizId, mode);
        session.lobbyCode = lobbyCode;
        session.state = state;
        session.activeTeamId = activeTeamId;
        session.currentBuzzedPlayerId = currentBuzzedPlayerId;
        session.activeClueId = activeClueId;
        session.buzzQueue.addAll(buzzQueue);
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
        this.state = GameState.BOARD_SELECTION;

        this.domainEvents.add(new GameStateChangedEvent(this.id, this.state));
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
        this.buzzQueue.clear();
        teams.values().forEach(team -> team.setLockedOut(false));
        this.state = GameState.CLUE_READING;

        this.domainEvents.add(new ClueSelectedEvent(this.id, clueId));
        this.domainEvents.add(new GameStateChangedEvent(this.id, this.state));
    }

    /** The host finished reading: buzzes count from now on. */
    public void openBuzzers() {
        if (state != GameState.CLUE_READING) {
            throw new IllegalGameStateTransitionException(state, "openBuzzers");
        }
        this.state = GameState.BUZZER_OPEN;
        this.domainEvents.add(new GameStateChangedEvent(this.id, this.state));
    }

    /**
     * Records a buzz in arrival order. Buzzes keep queueing while someone answers, so the next team is up
     * right away after a wrong answer. Returns false (no error) when the buzz doesn't count: buzzers closed,
     * too early, team locked out, or the team already buzzed. Players mash the button; that's fine.
     */
    public boolean registerBuzz(PlayerId playerId) {
        Player player = players.get(playerId);
        if (player == null) {
            throw new PlayerNotFoundException(playerId);
        }
        if (state != GameState.BUZZER_OPEN && state != GameState.ANSWER_EVALUATION) {
            return false;
        }

        TeamId teamId = player.getTeamId().orElseThrow(() -> new TeamNotFoundException(null));
        if (!canBuzz(teamId)) {
            return false;
        }

        buzzQueue.add(playerId);
        this.domainEvents.add(new BuzzerPressedEvent(this.id, playerId));

        if (state == GameState.BUZZER_OPEN) {
            this.currentBuzzedPlayerId = playerId;
            this.state = GameState.ANSWER_EVALUATION;
            this.domainEvents.add(new GameStateChangedEvent(this.id, this.state));
        }
        return true;
    }

    /** Whether a buzz from this team would count now (ignoring the game state). */
    public boolean canBuzz(TeamId teamId) {
        Team team = teams.get(teamId);
        if (team == null || team.isLockedOut()) {
            return false;
        }
        return buzzQueue.stream().noneMatch(queued -> teamId.equals(teamOf(queued)));
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
            buzzQueue.remove(answeringPlayerId);
            this.currentBuzzedPlayerId = buzzQueue.isEmpty() ? null : buzzQueue.getFirst();
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
        this.activeClueId = null;
        this.currentBuzzedPlayerId = null;
        this.buzzQueue.clear();
        teams.values().forEach(team -> team.setLockedOut(false));

        boolean allRevealed = clues.values().stream().allMatch(ClueState::isRevealed);
        this.state = allRevealed ? GameState.GAME_OVER : GameState.BOARD_SELECTION;
    }

    public List<DomainEvent> pullDomainEvents() {
        var copy = List.copyOf(domainEvents);
        domainEvents.clear();
        return copy;
    }

    public List<PlayerId> getBuzzQueue() { return Collections.unmodifiableList(buzzQueue); }
    public Collection<Player> getPlayers() { return Collections.unmodifiableCollection(players.values()); }
    public Collection<Team> getTeams() { return Collections.unmodifiableCollection(teams.values()); }
}