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
import dev.tanguy.game.jeopardy.gameplay.domain.exception.player.InvalidTurnException;
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
    private PlayerId currentBuzzedPlayerId;
    private ClueId activeClueId;

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
            List<ClueState> clues, List<Team> teams, List<Player> players
    ) {
        GameSession session = new GameSession(id, ownerId, quizId, mode);
        session.lobbyCode = lobbyCode;
        session.state = state;
        session.activeTeamId = activeTeamId;
        session.currentBuzzedPlayerId = currentBuzzedPlayerId;
        session.activeClueId = activeClueId;
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

    public void selectClue(PlayerId selector, ClueId clueId) {
        if (state != GameState.BOARD_SELECTION) {
            throw new IllegalGameStateTransitionException(state, "selectClue");
        }

        Player player = players.get(selector);
        if (player == null) {
            throw new PlayerNotFoundException(selector);
        }

        TeamId playerTeamId = player.getTeamId().orElseThrow(() -> new TeamNotFoundException(null));
        if (!playerTeamId.equals(activeTeamId)) {
            throw new InvalidTurnException(selector);
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
        this.state = GameState.CLUE_READING;

        this.domainEvents.add(new ClueSelectedEvent(this.id, selector, clueId));
        this.domainEvents.add(new GameStateChangedEvent(this.id, this.state));
    }

    public void openBuzzers() {
        if (state != GameState.CLUE_READING) {
            throw new IllegalGameStateTransitionException(state, "openBuzzers");
        }
        this.state = GameState.BUZZER_OPEN;
        this.domainEvents.add(new GameStateChangedEvent(this.id, this.state));
    }

    public boolean registerBuzz(PlayerId playerId) {
        if (state != GameState.BUZZER_OPEN) {
            return false;
        }

        if (!players.containsKey(playerId)) {
            throw new PlayerNotFoundException(playerId);
        }

        this.currentBuzzedPlayerId = playerId;
        this.state = GameState.ANSWER_EVALUATION;

        this.domainEvents.add(new BuzzerPressedEvent(this.id, playerId));
        this.domainEvents.add(new GameStateChangedEvent(this.id, this.state));

        return true;
    }

    public void evaluateAnswer(boolean isCorrect) {
        if (state != GameState.ANSWER_EVALUATION) {
            throw new IllegalGameStateTransitionException(state, "evaluateAnswer");
        }

        ClueState clue = clues.get(activeClueId);
        if (clue == null) {
            throw new ClueNotFoundException(activeClueId);
        }

        Player player = players.get(currentBuzzedPlayerId);
        if (player == null) {
            throw new PlayerNotFoundException(currentBuzzedPlayerId);
        }

        TeamId teamId = player.getTeamId().orElseThrow(() -> new TeamNotFoundException(null));
        Team team = teams.get(teamId);
        if (team == null) {
            throw new TeamNotFoundException(teamId);
        }

        if (isCorrect) {
            team.addScore(clue.getValue());
            this.activeTeamId = team.getId();
            resetToBoardSelection();
        } else {
            team.subtractScore(clue.getValue());
            this.currentBuzzedPlayerId = null;
            this.state = GameState.BUZZER_OPEN;
        }

        this.domainEvents.add(new AnswerEvaluatedEvent(this.id, currentBuzzedPlayerId, teamId, isCorrect, clue.getValue()));
        this.domainEvents.add(new GameStateChangedEvent(this.id, this.state));
    }

    private void resetToBoardSelection() {
        this.activeClueId = null;
        this.currentBuzzedPlayerId = null;

        boolean allRevealed = clues.values().stream().allMatch(ClueState::isRevealed);
        this.state = allRevealed ? GameState.GAME_OVER : GameState.BOARD_SELECTION;
    }

    public List<DomainEvent> pullDomainEvents() {
        var copy = List.copyOf(domainEvents);
        domainEvents.clear();
        return copy;
    }

    public Collection<Player> getPlayers() { return Collections.unmodifiableCollection(players.values()); }
    public Collection<Team> getTeams() { return Collections.unmodifiableCollection(teams.values()); }
}