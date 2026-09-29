-- Creator domain: Quiz -> Category -> Question
CREATE TABLE quizzes (
    id       UUID         PRIMARY KEY,
    owner_id UUID         NOT NULL,
    name     VARCHAR(255) NOT NULL
);
CREATE INDEX idx_quizzes_owner_id ON quizzes (owner_id);

CREATE TABLE categories (
    id         UUID         PRIMARY KEY,
    quiz_id    UUID         NOT NULL REFERENCES quizzes (id) ON DELETE CASCADE,
    name       VARCHAR(255) NOT NULL,
    sort_order INT          NOT NULL DEFAULT 0
);
CREATE INDEX idx_categories_quiz_id ON categories (quiz_id);

CREATE TABLE questions (
    id              UUID          PRIMARY KEY,
    category_id     UUID          NOT NULL REFERENCES categories (id) ON DELETE CASCADE,
    points          INT           NOT NULL,
    question_text   TEXT          NOT NULL,
    answer_text     TEXT          NOT NULL,
    answer_type     VARCHAR(20)   NOT NULL,
    media_url       VARCHAR(2048),
    is_daily_double BOOLEAN       NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_questions_category_id ON questions (category_id);
