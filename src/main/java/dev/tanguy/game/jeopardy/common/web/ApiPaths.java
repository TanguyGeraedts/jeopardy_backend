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
        public static final String END_GAME = BY_ID + "/end";
        public static final String TEAM_SCORE = BY_ID + "/teams/{teamId}/score";
        public static final String TEAM_BUZZER_SOUND = BY_ID + "/teams/{teamId}/buzzer-sound";
    }

    /** Everyone's published quizzes (plus your own), as board shapes without questions. */
    public static final class Discover {
        public static final String BASE = V1_BASE + "/discover";
        public static final String BY_ID = "/{id}";
    }

    /** Calls from other services. Under /api/public: each checks its own signature instead of a login. */
    public static final class Webhooks {
        public static final String CLERK = "/api/public/webhooks/clerk";
    }

    public static final class Creator {
        public static final String BASE = V1_BASE + "/quizzes";
        public static final String BY_ID = "/{id}";

        public static final String PUBLICATION = BY_ID + "/publication";
        public static final String TOPICS = BY_ID + "/topics";
        public static final String DUPLICATE = BY_ID + "/duplicate";

        public static final String CATEGORIES = BY_ID + "/categories";
        public static final String CATEGORY_BY_ID = CATEGORIES + "/{categoryId}";
        public static final String QUESTIONS = CATEGORY_BY_ID + "/questions";
        public static final String QUESTION_BY_ID = QUESTIONS + "/{questionId}";
    }
}