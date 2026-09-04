package dev.tanguy.game.jeopardy.gameplay.domain.model;

import dev.tanguy.game.jeopardy.common.domain.exception.DomainConflictException;
import dev.tanguy.game.jeopardy.common.domain.model.id.ClueId;
import dev.tanguy.game.jeopardy.common.domain.model.id.GameSessionId;
import dev.tanguy.game.jeopardy.common.domain.model.id.PlayerId;
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
import dev.tanguy.game.jeopardy.gameplay.domain.exception.session.IllegalGameStateTransitionException;
import dev.tanguy.game.jeopardy.gameplay.domain.exception.team.TeamNotFoundException;
import lombok.Getter;

import java.util.*;

@Getter
public class GameSession {

    private final GameSessionId id;
    private final Map<PlayerId, Player> players = new HashMap<>();
    private final Map<TeamId, Team> teams = new HashMap<>();
    private final Map<ClueId, ClueState> clues = new HashMap<>();
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    private GameState state = GameState.LOBBY;
    private TeamId activeTeamId;
    private PlayerId currentBuzzedPlayerId;
    private ClueId activeClueId;

    public GameSession(GameSessionId id, List<ClueState> initialClues) {
        this.id = id;
        initialClues.forEach(clue -> this.clues.put(clue.getId(), clue));
        this.domainEvents.add(new GameSessionCreatedEvent(this.id));
    }

    public void createTeam(TeamId teamId, String teamName) {
        if (state != GameState.LOBBY) {
            throw new IllegalGameStateTransitionException(state, "createTeam");
        }
        teams.put(teamId, new Team(teamId, teamName));
        this.domainEvents.add(new TeamCreatedEvent(this.id, teamId, teamName));
    }

    public void addPlayer(PlayerId playerId, String name, TeamId teamId) {
        if (state != GameState.LOBBY) {
            throw new IllegalGameStateTransitionException(state, "addPlayer");
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

        // Record events internally
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