package dev.tanguy.game.jeopardy.gameplay.core.session;

import dev.tanguy.game.jeopardy.common.domain.model.id.TeamId;
import dev.tanguy.game.jeopardy.common.events.DomainEventPublisher;
import dev.tanguy.game.jeopardy.gameplay.domain.exception.session.GameSessionNotFoundException;
import dev.tanguy.game.jeopardy.gameplay.domain.model.GameSession;
import dev.tanguy.game.jeopardy.gameplay.domain.model.Player;
import dev.tanguy.game.jeopardy.gameplay.domain.model.Team;
import dev.tanguy.game.jeopardy.gameplay.port.in.session.AddPlayerCommand;
import dev.tanguy.game.jeopardy.gameplay.port.in.session.AddPlayerResult;
import dev.tanguy.game.jeopardy.gameplay.port.in.session.AddPlayerUseCase;
import dev.tanguy.game.jeopardy.gameplay.port.out.session.LoadGameSessionPort;
import dev.tanguy.game.jeopardy.gameplay.port.out.session.SaveGameSessionPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AddPlayerUseCaseImpl implements AddPlayerUseCase {

    private final LoadGameSessionPort loadGameSessionPort;
    private final SaveGameSessionPort saveGameSessionPort;
    private final DomainEventPublisher domainEventPublisher;

    @Override
    public AddPlayerResult addPlayer(AddPlayerCommand command) {
        GameSession session = loadGameSessionPort.loadGameSessionById(command.sessionId())
                .orElseThrow(() -> new GameSessionNotFoundException(command.sessionId()));


        Optional<Player> existing = session.getPlayers().stream()
                .filter(p -> p.id().equals(command.playerId()))
                .findFirst();
        if (existing.isPresent()) {
            return new AddPlayerResult(command.playerId(), existing.get().teamId(), true);
        }

        TeamId teamId = null;
        if (command.externalTeamId() != null) {
            Team team = session.findTeamByExternalId(command.externalTeamId())
                    .orElseGet(() -> session.createTeam(
                            TeamId.generate(), command.teamName(), command.externalTeamId(), command.teamColour()));
            teamId = team.getId();
        }

        session.addPlayer(command.playerId(), command.playerName(), teamId);

        saveGameSessionPort.saveGameSession(session);
        domainEventPublisher.publishAll(session.pullDomainEvents());

        TeamId assignedTeamId = session.getPlayers().stream()
                .filter(p -> p.id().equals(command.playerId()))
                .findFirst()
                .map(Player::teamId)
                .orElseThrow(() -> new IllegalStateException(
                        "Player " + command.playerId() + " was added but not found in session " + session.getId()));

        return new AddPlayerResult(command.playerId(), assignedTeamId, false);
    }
}