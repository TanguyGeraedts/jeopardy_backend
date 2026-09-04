package dev.tanguy.game.jeopardy.gameplay.domain.exception.session;

import dev.tanguy.game.jeopardy.common.domain.exception.NotFoundException;
import dev.tanguy.game.jeopardy.common.domain.model.id.GameSessionId;

public class GameSessionNotFoundException extends NotFoundException {

    public GameSessionNotFoundException(GameSessionId id) {
        super("Game session not found with ID: " + id.value());
    }
}