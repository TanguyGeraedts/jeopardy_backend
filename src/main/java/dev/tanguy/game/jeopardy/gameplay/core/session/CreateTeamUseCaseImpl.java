package dev.tanguy.game.jeopardy.gameplay.core.session;

import dev.tanguy.game.jeopardy.common.domain.model.id.TeamId;
import dev.tanguy.game.jeopardy.common.events.DomainEventPublisher;
import dev.tanguy.game.jeopardy.gameplay.domain.exception.session.GameSessionNotFoundException;
import dev.tanguy.game.jeopardy.gameplay.domain.model.GameSession;
import dev.tanguy.game.jeopardy.gameplay.port.in.session.CreateTeamCommand;
import dev.tanguy.game.jeopardy.gameplay.port.in.session.CreateTeamUseCase;
import dev.tanguy.game.jeopardy.gameplay.port.out.session.LoadGameSessionPort;
import dev.tanguy.game.jeopardy.gameplay.port.out.session.SaveGameSessionPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateTeamUseCaseImpl implements CreateTeamUseCase {

    private final LoadGameSessionPort loadGameSessionPort;
    private final SaveGameSessionPort saveGameSessionPort;
    private final DomainEventPublisher domainEventPublisher;

    @Override
    public TeamId createTeam(CreateTeamCommand command) {
        GameSession session = loadGameSessionPort.loadGameSessionById(command.sessionId())
                .orElseThrow(() -> new GameSessionNotFoundException(command.sessionId()));

        TeamId teamId = TeamId.generate();
        session.createTeam(teamId, command.teamName());

        saveGameSessionPort.saveGameSession(session);
        domainEventPublisher.publishAll(session.pullDomainEvents());

        return teamId;
    }
}