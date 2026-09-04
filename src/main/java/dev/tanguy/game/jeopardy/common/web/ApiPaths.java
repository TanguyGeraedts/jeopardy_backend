package dev.tanguy.game.jeopardy.common.web;

public final class ApiPaths {

    private ApiPaths() {}

    public static final String V1_BASE = "/api/v1";

    public static final class Gameplay {
        public static final String BASE = V1_BASE + "/game-sessions";
        public static final String BY_ID = "/{id}";
        public static final String ANSWER = BY_ID + "/answer";
        public static final String JOIN = BY_ID + "/join";
    }
}