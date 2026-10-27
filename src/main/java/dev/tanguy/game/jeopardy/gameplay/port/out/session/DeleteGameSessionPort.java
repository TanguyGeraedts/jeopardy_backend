package dev.tanguy.game.jeopardy.gameplay.port.out.session;

import dev.tanguy.game.jeopardy.common.domain.model.id.GameSessionId;
import dev.tanguy.game.jeopardy.common.domain.model.id.OwnerId;

import java.time.Instant;

public interface DeleteGameSessionPort {

    void deleteGameSessionById(GameSessionId id);

    /** Every game this owner hosted, with its players, teams and scores. Returns how many were deleted. */
    int deleteGameSessionsByOwner(OwnerId ownerId);

    /** Every game created before the cutoff, with its players, teams and scores. Returns how many were deleted. */
    int deleteGameSessionsCreatedBefore(Instant cutoff);
}
