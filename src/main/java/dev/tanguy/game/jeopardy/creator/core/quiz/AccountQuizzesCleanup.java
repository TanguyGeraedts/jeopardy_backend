package dev.tanguy.game.jeopardy.creator.core.quiz;

import dev.tanguy.game.jeopardy.common.events.UserAccountDeletedEvent;
import dev.tanguy.game.jeopardy.creator.port.out.QuizRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Deleting an account deletes all its quizzes, published ones included. Runs inside the webhook request, so a failure
 * makes Clerk retry; deleting again finds nothing, which is fine.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountQuizzesCleanup {

    private final QuizRepositoryPort quizRepositoryPort;

    @EventListener
    @Transactional
    public void on(UserAccountDeletedEvent event) {
        int deleted = quizRepositoryPort.deleteByOwner(event.ownerId());
        log.info("Deleted {} quizzes of deleted account {}", deleted, event.ownerId().value());
    }
}
