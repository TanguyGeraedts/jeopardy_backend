package dev.tanguy.game.jeopardy.gameplay.port.in.session;

public interface BuzzUseCase {

    /** True if the buzz counted, false if it was ignored (buzzers closed, locked out, already in line). */
    boolean buzz(BuzzCommand command);
}
