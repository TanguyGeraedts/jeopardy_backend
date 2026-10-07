package dev.tanguy.game.jeopardy.gameplay.core.session;

import dev.tanguy.game.jeopardy.common.domain.model.id.PlayerId;
import dev.tanguy.game.jeopardy.common.domain.model.id.TeamId;
import dev.tanguy.game.jeopardy.common.events.DomainEventPublisher;
import dev.tanguy.game.jeopardy.gameplay.domain.exception.session.GameSessionNotFoundException;
import dev.tanguy.game.jeopardy.gameplay.domain.exception.session.IllegalGameStateTransitionException;
import dev.tanguy.game.jeopardy.gameplay.domain.exception.session.InvalidHandoffException;
import dev.tanguy.game.jeopardy.gameplay.domain.model.GameSession;
import dev.tanguy.game.jeopardy.gameplay.domain.model.GameState;
import dev.tanguy.game.jeopardy.gameplay.domain.model.Team;
import dev.tanguy.game.jeopardy.gameplay.port.in.session.StartGameFromLobbyCommand;
import dev.tanguy.game.jeopardy.gameplay.port.in.session.StartGameFromLobbyUseCase;
import dev.tanguy.game.jeopardy.gameplay.port.out.lobby.LobbyHandoff;
import dev.tanguy.game.jeopardy.gameplay.port.out.lobby.LobbyPort;
import dev.tanguy.game.jeopardy.gameplay.port.out.session.LoadGameSessionPort;
import dev.tanguy.game.jeopardy.gameplay.port.out.session.SaveGameSessionPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StartGameFromLobbyUseCaseImpl implements StartGameFromLobbyUseCase {

    private final LoadGameSessionPort loadGameSessionPort;
    private final SaveGameSessionPort saveGameSessionPort;
    private final LobbyPort lobbyPort;
    private final DomainEventPublisher domainEventPublisher;

    @Override
    public GameSession startFromHandoff(StartGameFromLobbyCommand command) {
        // No login needed: the lobby's signed handoff token is the credential (checked below).
        GameSession session = loadGameSessionPort.loadGameSessionById(command.sessionId())
                .orElseThrow(() -> new GameSessionNotFoundException(command.sessionId()));

        // A replayed handoff must not restart a running game.
        if (session.getState() != GameState.LOBBY) {
            throw new IllegalGameStateTransitionException(session.getState(), "startFromLobby");
        }

        LobbyHandoff handoff = lobbyPort.verifyHandoff(command.handoffToken());

        // The token's audience is generic ("game-server"), so make sure it is OUR lobby's handoff.
        if (session.getLobbyCode() == null || !session.getLobbyCode().equals(handoff.lobbyCode())) {
            throw new InvalidHandoffException("This handoff belongs to a different lobby.");
        }

        Map<Integer, LobbyHandoff.Team> lobbyTeams = new HashMap<>();
        handoff.teams().forEach(team -> lobbyTeams.put(team.teamId(), team));

        for (LobbyHandoff.Player lobbyPlayer : handoff.players()) {
            TeamId teamId = null;
            if (lobbyPlayer.teamId() != null) {
                String externalTeamId = String.valueOf(lobbyPlayer.teamId());
                LobbyHandoff.Team lobbyTeam = lobbyTeams.get(lobbyPlayer.teamId());
                Team team = session.findTeamByExternalId(externalTeamId)
                        .orElseGet(() -> session.createTeam(
                                TeamId.generate(),
                                lobbyTeam != null ? lobbyTeam.name() : "Team " + externalTeamId,
                                externalTeamId,
                                lobbyTeam != null ? lobbyTeam.colour() : null));
                teamId = team.getId();
            }

            session.addPlayer(new PlayerId(lobbyPlayer.playerId()), lobbyPlayer.username(), teamId);
        }

        session.startGame();

        saveGameSessionPort.saveGameSession(session);
        domainEventPublisher.publishAll(session.pullDomainEvents());

        return session;
    }
}