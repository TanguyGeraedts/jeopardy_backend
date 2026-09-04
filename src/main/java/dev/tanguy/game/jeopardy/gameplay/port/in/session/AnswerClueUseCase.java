package dev.tanguy.game.jeopardy.gameplay.port.in.session;

public interface AnswerClueUseCase {
    AnswerClueResult answerClue(AnswerClueCommand command);
}
