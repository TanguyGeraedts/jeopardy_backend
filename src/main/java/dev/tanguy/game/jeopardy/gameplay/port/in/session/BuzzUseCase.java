package dev.tanguy.game.jeopardy.gameplay.port.in.session;

import dev.tanguy.game.jeopardy.gameplay.domain.model.BuzzOutcome;

public interface BuzzUseCase {

    /**
     * ACCEPTED: in line. TOO_EARLY: pressed before the buzzers opened (or during another team's head start), or while
     * still locked out for that: the player is locked out for BuzzerLockouts.LOCKOUT. IGNORED: doesn't count, no penalty.
     */
    BuzzOutcome buzz(BuzzCommand command);
}
