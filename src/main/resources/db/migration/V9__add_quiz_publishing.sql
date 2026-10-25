-- Discover: published quizzes anyone signed in can find and host, with topics, an author name and a play count.
ALTER TABLE quizzes
    ADD COLUMN published    BOOLEAN     NOT NULL DEFAULT FALSE,
    ADD COLUMN published_at TIMESTAMPTZ,
    ADD COLUMN author_name  VARCHAR(60),
    -- Games hosted with this quiz (by anyone). Only ever changed by an increment query.
    ADD COLUMN times_played INT         NOT NULL DEFAULT 0,
    ADD COLUMN created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    ADD COLUMN updated_at   TIMESTAMPTZ NOT NULL DEFAULT now();

CREATE INDEX idx_quizzes_published ON quizzes (published_at DESC) WHERE published;

-- Up to 3 per quiz (checked by the Quiz aggregate). Values are QuizTopic names.
CREATE TABLE quiz_topics (
    quiz_id UUID        NOT NULL REFERENCES quizzes (id) ON DELETE CASCADE,
    topic   VARCHAR(30) NOT NULL,
    PRIMARY KEY (quiz_id, topic)
);
CREATE INDEX idx_quiz_topics_topic ON quiz_topics (topic);
