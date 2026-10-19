package dev.tanguy.game.jeopardy.gameplay.core.session;

import dev.tanguy.game.jeopardy.common.domain.model.id.PlayerId;
import dev.tanguy.game.jeopardy.gameplay.domain.exception.player.InvalidPlayerTokenException;
import dev.tanguy.game.jeopardy.gameplay.port.in.session.BuzzCommand;
import dev.tanguy.game.jeopardy.gameplay.port.in.session.BuzzUseCase;
import dev.tanguy.game.jeopardy.gameplay.port.out.lobby.LobbyPlayerIdentity;
import dev.tanguy.game.jeopardy.gameplay.port.out.lobby.LobbyPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BuzzUseCaseImpl implements BuzzUseCase {

    private final LobbyPort lobbyPort;
    private final GameSessionUpdater updater;

    @Override
    public boolean buzz(BuzzCommand command) {
        // Verify outside the lock: it's CPU work and doesn't need the session.
        LobbyPlayerIdentity identity = lobbyPort.verifyPlayerToken(command.playerToken());
        PlayerId playerId = new PlayerId(identity.playerId());

        return updater.updateIf(command.sessionId(), session -> {
            // The token is generic per lobby: make sure it is for THIS game and a player in it.
            if (session.getLobbyCode() == null || !session.getLobbyCode().equals(identity.lobbyCode())
                    || session.findPlayer(playerId).isEmpty()) {
                throw new InvalidPlayerTokenException("This player token is not valid for this game.");
            }
            return session.registerBuzz(playerId);
        });
    }
}
