-- Gameplay domain: a game session owns a snapshot of the quiz (clues), plus teams and players.
-- quiz_id is deliberately NOT a foreign key: a running game must survive edits/deletes of the quiz.
CREATE TABLE game_sessions (
                               id                       UUID        PRIMARY KEY,
                               owner_id                 UUID        NOT NULL,
                               quiz_id                  UUID        NOT NULL,
    -- Room code issued by the lobby microservice. Null until the lobby has been created.
                               lobby_code               VARCHAR(64),
                               mode                     VARCHAR(10) NOT NULL,
                               state                    VARCHAR(30) NOT NULL,
                               active_team_id           UUID,
                               current_buzzed_player_id UUID,
                               active_clue_id           UUID,
                               created_at               TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_game_sessions_owner_id ON game_sessions (owner_id);
-- Not unique: the lobby may recycle codes once an old room is closed.
CREATE INDEX idx_game_sessions_lobby_code ON game_sessions (lobby_code);

CREATE TABLE game_session_clues (
                                    id              UUID         PRIMARY KEY,
                                    game_session_id UUID         NOT NULL REFERENCES game_sessions (id) ON DELETE CASCADE,
                                    sort_order      INT          NOT NULL,
                                    category_name   VARCHAR(255) NOT NULL,
                                    points          INT          NOT NULL,
                                    question_text   TEXT         NOT NULL,
                                    answer_text     TEXT         NOT NULL,
                                    is_daily_double BOOLEAN      NOT NULL DEFAULT FALSE,
                                    is_revealed     BOOLEAN      NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_game_session_clues_session ON game_session_clues (game_session_id);

CREATE TABLE game_session_teams (
                                    id               UUID         PRIMARY KEY,
                                    game_session_id  UUID         NOT NULL REFERENCES game_sessions (id) ON DELETE CASCADE,
                                    name             VARCHAR(255) NOT NULL,
                                    external_team_id VARCHAR(255),
                                    colour           VARCHAR(50),
                                    score            INT          NOT NULL DEFAULT 0
);
CREATE INDEX idx_game_session_teams_session ON game_session_teams (game_session_id);

-- id is a row id derived from (session, player): the same person can play in several sessions.
CREATE TABLE game_session_players (
                                      id              UUID         PRIMARY KEY,
                                      game_session_id UUID         NOT NULL REFERENCES game_sessions (id) ON DELETE CASCADE,
                                      player_id       UUID         NOT NULL,
                                      name            VARCHAR(255) NOT NULL,
                                      team_id         UUID,
                                      UNIQUE (game_session_id, player_id)
);
CREATE INDEX idx_game_session_players_session ON game_session_players (game_session_id);