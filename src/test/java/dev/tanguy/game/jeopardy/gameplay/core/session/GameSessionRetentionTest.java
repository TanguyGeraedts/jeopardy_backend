package dev.tanguy.game.jeopardy.gameplay.core.session;

import dev.tanguy.game.jeopardy.common.domain.model.id.OwnerId;
import dev.tanguy.game.jeopardy.common.events.UserAccountDeletedEvent;
import dev.tanguy.game.jeopardy.gameplay.port.out.session.DeleteGameSessionPort;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class GameSessionRetentionTest {

    static final Instant NOW = Instant.parse("2026-10-27T03:30:00Z");

    final DeleteGameSessionPort port = mock(DeleteGameSessionPort.class);
    final GameSessionRetention retention = new GameSessionRetention(port, Duration.ofDays(30), Clock.fixed(NOW, ZoneOffset.UTC));

    @Test
    void nightlyJobDeletesGamesOlderThanTheRetention() {
        retention.deleteExpiredSessions();

        verify(port).deleteGameSessionsCreatedBefore(Instant.parse("2026-09-27T03:30:00Z"));
    }

    @Test
    void deletedAccountLosesTheGamesItHosted() {
        OwnerId owner = OwnerId.generate();

        retention.on(new UserAccountDeletedEvent(owner, NOW));

        verify(port).deleteGameSessionsByOwner(owner);
    }
}
