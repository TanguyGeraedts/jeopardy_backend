-- Buzzers: who buzzed on the open clue (arrival order) and the host's block switches.
ALTER TABLE game_sessions
    ADD COLUMN buzzers_blocked BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE game_session_teams
    -- Answered the open clue wrongly: can't buzz again until the next clue.
    ADD COLUMN locked_out     BOOLEAN NOT NULL DEFAULT FALSE,
    -- Blocked by the host until unblocked.
    ADD COLUMN buzzer_blocked BOOLEAN NOT NULL DEFAULT FALSE;

-- One row per buzz on the open clue; cleared when the clue closes. position 0 = answering (or first in line).
CREATE TABLE game_session_buzzes (
    game_session_id UUID NOT NULL REFERENCES game_sessions (id) ON DELETE CASCADE,
    position        INT  NOT NULL,
    player_id       UUID NOT NULL,
    PRIMARY KEY (game_session_id, position)
);
