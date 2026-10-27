package dev.tanguy.game.jeopardy.gameplay.core.session;

import dev.tanguy.game.jeopardy.common.events.UserAccountDeletedEvent;
import dev.tanguy.game.jeopardy.gameplay.port.out.session.DeleteGameSessionPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

/**
 * How long game data (players' names, teams, scores, buzzes) is kept, as promised in the privacy notice:
 * a nightly job deletes games older than app.gameplay.session-retention, and deleting an account deletes the games
 * it hosted right away.
 */
@Slf4j
@Service
public class GameSessionRetention {

    private final DeleteGameSessionPort deleteGameSessionPort;
    private final Duration retention;
    private final Clock clock;

    @Autowired
    public GameSessionRetention(DeleteGameSessionPort deleteGameSessionPort,
                                @Value("${app.gameplay.session-retention:30d}") Duration retention) {
        this(deleteGameSessionPort, retention, Clock.systemUTC());
    }

    GameSessionRetention(DeleteGameSessionPort deleteGameSessionPort, Duration retention, Clock clock) {
        this.deleteGameSessionPort = deleteGameSessionPort;
        this.retention = retention;
        this.clock = clock;
    }

    @Scheduled(cron = "${app.gameplay.session-cleanup-cron:0 30 3 * * *}", zone = "UTC")
    public void deleteExpiredSessions() {
        Instant cutoff = clock.instant().minus(retention);
        int deleted = deleteGameSessionPort.deleteGameSessionsCreatedBefore(cutoff);
        log.info("Deleted {} game sessions created before {}", deleted, cutoff);
    }

    @EventListener
    public void on(UserAccountDeletedEvent event) {
        int deleted = deleteGameSessionPort.deleteGameSessionsByOwner(event.ownerId());
        log.info("Deleted {} game sessions hosted by deleted account {}", deleted, event.ownerId().value());
    }
}
