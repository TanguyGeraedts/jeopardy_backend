package dev.tanguy.game.jeopardy.creator.domain.model;

/** What a quiz is about, for filtering Discover. A fixed list on purpose: free tags would split "movies" and "film". */
public enum QuizTopic {
    GENERAL_KNOWLEDGE,
    MOVIES_TV,
    MUSIC,
    SPORTS,
    SCIENCE_NATURE,
    HISTORY,
    GEOGRAPHY,
    GAMES,
    FOOD_DRINK,
    ART_LITERATURE,
    TECHNOLOGY,
    POP_CULTURE,
    LANGUAGE_WORDS,
    KIDS_FAMILY
}
