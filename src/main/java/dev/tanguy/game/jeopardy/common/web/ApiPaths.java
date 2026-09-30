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

    public static final class Creator {
        public static final String BASE = V1_BASE + "/quizzes";
        public static final String BY_ID = "/{id}";

        public static final String CATEGORIES = BY_ID + "/categories";
        public static final String CATEGORY_BY_ID = CATEGORIES + "/{categoryId}";
        public static final String QUESTIONS = CATEGORY_BY_ID + "/questions";
        public static final String QUESTION_BY_ID = QUESTIONS + "/{questionId}";
    }
}