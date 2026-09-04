package dev.tanguy.game.jeopardy.gameplay.port.out.board;

import dev.tanguy.game.jeopardy.gameplay.domain.model.ClueState;

import java.util.List;

public interface LoadBoardTemplatePort {

    List<ClueState> loadCluesForBoard(String boardId);
}