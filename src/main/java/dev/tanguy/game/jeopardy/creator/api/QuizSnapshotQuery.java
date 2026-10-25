package dev.tanguy.game.jeopardy.creator.api;

import dev.tanguy.game.jeopardy.common.domain.model.id.OwnerId;
import dev.tanguy.game.jeopardy.common.domain.model.id.QuizId;

/** Read-only view of a quiz for other modules: the requester's own quiz or a published one, else QuizNotFoundException. */
public interface QuizSnapshotQuery {

    QuizSnapshot getSnapshot(QuizId quizId, OwnerId requesterId);
}