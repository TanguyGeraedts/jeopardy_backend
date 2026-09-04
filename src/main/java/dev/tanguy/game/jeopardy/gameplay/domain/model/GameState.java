package dev.tanguy.game.jeopardy.gameplay.domain.model;

public enum GameState {
    LOBBY,
    BOARD_SELECTION,
    CLUE_READING,
    BUZZER_OPEN,
    ANSWER_EVALUATION,
    GAME_OVER
}