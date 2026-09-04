package dev.tanguy.game.jeopardy.gameplay.core.session;

import dev.tanguy.game.jeopardy.common.domain.model.id.GameSessionId;
import dev.tanguy.game.jeopardy.common.events.DomainEventPublisher;
import dev.tanguy.game.jeopardy.gameplay.domain.model.ClueState;
import dev.tanguy.game.jeopardy.gameplay.domain.model.GameSession;
import dev.tanguy.game.jeopardy.gameplay.port.in.session.CreateGameSessionCommand;
import dev.tanguy.game.jeopardy.gameplay.port.in.session.CreateGameSessionUseCase;
import dev.tanguy.game.jeopardy.gameplay.port.out.board.LoadBoardTemplatePort;
import dev.tanguy.game.jeopardy.gameplay.port.out.session.SaveGameSessionPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CreateGameSessionUseCaseImpl implements CreateGameSessionUseCase {

    private final LoadBoardTemplatePort loadBoardTemplatePort;
    private final SaveGameSessionPort saveGameSessionPort;
    private final DomainEventPublisher domainEventPublisher;

    @Override
    public GameSessionId createGameSession(CreateGameSessionCommand command) {
        List<ClueState> initialClues = loadBoardTemplatePort.loadCluesForBoard(command.boardId());

        GameSessionId newSessionId = GameSessionId.generate();
        GameSession session = new GameSession(newSessionId, initialClues, command.mode());

        saveGameSessionPort.saveGameSession(session);
        domainEventPublisher.publishAll(session.pullDomainEvents());

        return newSessionId;
    }
}