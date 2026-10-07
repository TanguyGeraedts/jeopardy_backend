package dev.tanguy.game.jeopardy.gameplay.domain.exception.session;

import dev.tanguy.game.jeopardy.common.domain.exception.DomainConflictException;
import dev.tanguy.game.jeopardy.common.domain.model.id.GameSessionId;

public class LobbyCodeAlreadyAssignedException extends DomainConflictException {

    public LobbyCodeAlreadyAssignedException(GameSessionId sessionId) {
        super("Game session " + sessionId.value() + " already has a lobby code.");
    }
}