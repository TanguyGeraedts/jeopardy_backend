package dev.tanguy.game.jeopardy.gameplay.core.session;

import dev.tanguy.game.jeopardy.common.domain.model.id.PlayerId;
import dev.tanguy.game.jeopardy.gameplay.domain.model.BuzzOutcome;
import dev.tanguy.game.jeopardy.gameplay.domain.exception.player.InvalidPlayerTokenException;
import dev.tanguy.game.jeopardy.gameplay.port.in.session.BuzzCommand;
import dev.tanguy.game.jeopardy.gameplay.port.in.session.BuzzUseCase;
import dev.tanguy.game.jeopardy.gameplay.port.out.lobby.LobbyPlayerIdentity;
import dev.tanguy.game.jeopardy.gameplay.port.out.lobby.LobbyPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

@Service
@RequiredArgsConstructor
public class BuzzUseCaseImpl implements BuzzUseCase {

    private final LobbyPort lobbyPort;
    private final GameSessionUpdater updater;
    private final BuzzerLockouts lockouts;

    @Override
    public BuzzOutcome buzz(BuzzCommand command) {
        // Timestamp on arrival: waiting for the session lock (other buzzes, DB saves) must not count against the player.
        Instant receivedAt = Instant.now();
        // Verify outside the lock: it's CPU work and doesn't need the session.
        LobbyPlayerIdentity identity = lobbyPort.verifyPlayerToken(command.playerToken());
        PlayerId playerId = new PlayerId(identity.playerId());

        // Still locked out for pressing early: pressing again keeps the lockout going, so mashing never gets through.
        if (lockouts.isLocked(command.sessionId(), playerId, receivedAt)) {
            lockouts.lock(command.sessionId(), playerId, receivedAt);
            return BuzzOutcome.TOO_EARLY;
        }

        AtomicReference<BuzzOutcome> outcome = new AtomicReference<>(BuzzOutcome.IGNORED);
        updater.updateIf(command.sessionId(), session -> {
            // The token is generic per lobby: make sure it is for THIS game and a player in it.
            if (session.getLobbyCode() == null || !session.getLobbyCode().equals(identity.lobbyCode())
                    || session.findPlayer(playerId).isEmpty()) {
                throw new InvalidPlayerTokenException("This player token is not valid for this game.");
            }
            outcome.set(session.registerBuzz(playerId, receivedAt));
            // Only an accepted buzz changes the game; a too-early press only locks the player out (in memory).
            return outcome.get() == BuzzOutcome.ACCEPTED;
        });

        if (outcome.get() == BuzzOutcome.TOO_EARLY) {
            lockouts.lock(command.sessionId(), playerId, receivedAt);
        }
        return outcome.get();
    }
}
