package dev.tanguy.game.jeopardy.gameplay.port.in.session;

public interface AddPlayerUseCase {
    AddPlayerResult addPlayer(AddPlayerCommand command);
}