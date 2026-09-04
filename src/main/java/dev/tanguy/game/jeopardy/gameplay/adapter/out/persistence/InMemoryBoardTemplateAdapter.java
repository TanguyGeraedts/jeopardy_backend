package dev.tanguy.game.jeopardy.gameplay.adapter.out.persistence;

import dev.tanguy.game.jeopardy.common.domain.model.id.ClueId;
import dev.tanguy.game.jeopardy.gameplay.domain.model.ClueState;
import dev.tanguy.game.jeopardy.gameplay.port.out.board.LoadBoardTemplatePort;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InMemoryBoardTemplateAdapter implements LoadBoardTemplatePort {

    @Override
    public List<ClueState> loadCluesForBoard(String boardId) {
        return List.of(
                new ClueState(ClueId.generate(), 200, "What is Java?", "A programming language", false),
                new ClueState(ClueId.generate(), 400, "What is Spring Boot?", "A Java framework", false),
                new ClueState(ClueId.generate(), 600, "What is Jeopardy?", "A game show", true)
        );
    }
}