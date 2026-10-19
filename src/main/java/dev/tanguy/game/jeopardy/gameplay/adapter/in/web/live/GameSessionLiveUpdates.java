package dev.tanguy.game.jeopardy.gameplay.adapter.in.web.live;

import dev.tanguy.game.jeopardy.common.domain.model.id.GameSessionId;
import dev.tanguy.game.jeopardy.gameplay.adapter.in.web.mapper.GameSessionResponseMapper;
import dev.tanguy.game.jeopardy.gameplay.domain.model.GameSession;
import dev.tanguy.game.jeopardy.gameplay.port.out.session.GameSessionUpdatesPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Server-Sent Events per game session. Every change sends a "session" event with the public view of the
 * whole game (no answers), so a client never has to merge partial updates. In memory: one backend instance.
 */
@Slf4j
@Component
public class GameSessionLiveUpdates implements GameSessionUpdatesPort {

    public static final String EVENT_NAME = "session";

    /** Clients reconnect on their own (EventSource), so a long timeout just limits stale connections. */
    private static final long TIMEOUT_MS = 30 * 60 * 1000L;

    private final Map<GameSessionId, List<SseEmitter>> subscribers = new ConcurrentHashMap<>();

    /** Subscribes and immediately sends the current state, so a (re)connecting client is up to date. */
    public SseEmitter subscribe(GameSession current) {
        GameSessionId sessionId = current.getId();
        SseEmitter emitter = new SseEmitter(TIMEOUT_MS);
        List<SseEmitter> emitters = subscribers.computeIfAbsent(sessionId, id -> new CopyOnWriteArrayList<>());
        emitters.add(emitter);

        Runnable remove = () -> emitters.remove(emitter);
        emitter.onCompletion(remove);
        emitter.onTimeout(remove);
        emitter.onError(error -> remove.run());

        send(emitter, current);
        return emitter;
    }

    @Override
    public void sessionChanged(GameSession session) {
        List<SseEmitter> emitters = subscribers.get(session.getId());
        if (emitters == null || emitters.isEmpty()) {
            return;
        }
        emitters.forEach(emitter -> {
            if (!send(emitter, session)) {
                emitters.remove(emitter);
            }
        });
    }

    private boolean send(SseEmitter emitter, GameSession session) {
        try {
            emitter.send(SseEmitter.event()
                    .name(EVENT_NAME)
                    .data(GameSessionResponseMapper.toPublicResponse(session), MediaType.APPLICATION_JSON));
            return true;
        } catch (IOException | IllegalStateException e) {
            log.debug("Dropping a live subscriber of game session {}: {}", session.getId().value(), e.getMessage());
            emitter.completeWithError(e);
            return false;
        }
    }
}
