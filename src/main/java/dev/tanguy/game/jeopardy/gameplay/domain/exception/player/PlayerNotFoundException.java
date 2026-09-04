package dev.tanguy.game.jeopardy.gameplay.domain.exception.player;

import dev.tanguy.game.jeopardy.common.domain.exception.NotFoundException;
import dev.tanguy.game.jeopardy.common.domain.model.id.PlayerId;

public class PlayerNotFoundException extends NotFoundException {
    public PlayerNotFoundException(PlayerId id) {
        super("Player not found with ID: " + id.value());
    }
}