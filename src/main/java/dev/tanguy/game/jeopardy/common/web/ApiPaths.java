package dev.tanguy.game.jeopardy.common.web;

public final class ApiPaths {

    private ApiPaths() {}

    public static final String V1_BASE = "/api/v1";

    public static final class Gameplay {
        public static final String BASE = V1_BASE + "/game-sessions";
        public static final String BY_ID = "/{id}";
        public static final String ANSWER = BY_ID + "/answer";
        public static final String JOIN = BY_ID + "/join";
        public static final String HANDOFF = BY_ID + "/handoff";

        /** Live updates (SSE). Public: answers are left out. */
        public static final String EVENTS = BY_ID + "/events";
        /** A player buzzes. Public: the lobby's player token (X-Player-Token) is the credential. */
        public static final String BUZZ = BY_ID + "/buzz";

        // Host (session owner) controls
        public static final String SELECT_CLUE = BY_ID + "/clues/{clueId}/select";
        public static final String OPEN_BUZZERS = BY_ID + "/buzzers/open";
        public static final String JUDGE = BY_ID + "/judge";
        public static final String CLOSE_CLUE = BY_ID + "/clue/close";
        public static final String TEAM_SCORE = BY_ID + "/teams/{teamId}/score";
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