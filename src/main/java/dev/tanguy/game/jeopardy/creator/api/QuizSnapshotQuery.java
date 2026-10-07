package dev.tanguy.game.jeopardy.creator.api;

import dev.tanguy.game.jeopardy.common.domain.model.id.OwnerId;
import dev.tanguy.game.jeopardy.common.domain.model.id.QuizId;

/** Read-only view of a quiz for other modules. Fails with QuizNotFoundException if the requester doesn't own it. */
public interface QuizSnapshotQuery {

    QuizSnapshot getSnapshot(QuizId quizId, OwnerId requesterId);
}