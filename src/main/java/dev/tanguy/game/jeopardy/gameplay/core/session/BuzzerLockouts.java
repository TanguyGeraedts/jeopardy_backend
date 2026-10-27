package dev.tanguy.game.jeopardy.gameplay.core.session;

import dev.tanguy.game.jeopardy.common.domain.model.id.GameSessionId;
import dev.tanguy.game.jeopardy.common.domain.model.id.PlayerId;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Players who pressed too early can't buzz for a moment (like the show's buzzer lockout), so mashing doesn't pay.
 * In memory on purpose: it lasts a second, and saving the whole game for every mashed press would be far too heavy.
 * Lost on a restart, which is harmless. With more than one backend instance this would need a shared store.
 */
@Component
public class BuzzerLockouts {

    public static final Duration LOCKOUT = Duration.ofSeconds(1);
    /** Expired entries are swept once the map grows past this. */
    private static final int SWEEP_ABOVE = 500;

    private record Key(GameSessionId sessionId, PlayerId playerId) {}

    private final Map<Key, Instant> lockedUntil = new ConcurrentHashMap<>();

    public boolean isLocked(GameSessionId sessionId, PlayerId playerId, Instant at) {
        Instant until = lockedUntil.get(new Key(sessionId, playerId));
        return until != null && at.isBefore(until);
    }

    /** Locks (or keeps locking: a press while locked counts as early again) until `at` + LOCKOUT. */
    public void lock(GameSessionId sessionId, PlayerId playerId, Instant at) {
        if (lockedUntil.size() > SWEEP_ABOVE) {
            lockedUntil.values().removeIf(until -> until.isBefore(at));
        }
        lockedUntil.put(new Key(sessionId, playerId), at.plus(LOCKOUT));
    }
}
