package dev.tanguy.game.jeopardy.gameplay.port.out.session;

import dev.tanguy.game.jeopardy.gameplay.domain.model.GameSession;

/** Pushes a saved session to everyone watching it live (board, admin, phones). */
public interface GameSessionUpdatesPort {

    /** Call after the session is saved. Best-effort: must never throw. */
    void sessionChanged(GameSession session);
}
