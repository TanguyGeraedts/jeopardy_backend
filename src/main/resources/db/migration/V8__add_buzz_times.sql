-- Buzz times: screens show the first buzz's reaction time and how far behind the others were.
ALTER TABLE game_sessions
    -- When the host opened the buzzers on the open clue. Null while it is being read.
    ADD COLUMN buzzers_opened_at TIMESTAMPTZ;

-- Buzzes of locked-out teams now stay in this table (shown crossed out) until the clue closes.
ALTER TABLE game_session_buzzes
    ADD COLUMN buzzed_at TIMESTAMPTZ NOT NULL DEFAULT now();

ALTER TABLE game_session_buzzes
    ALTER COLUMN buzzed_at DROP DEFAULT;

-- A clue open during the upgrade: its buzzers count as opened now (same moment as the backfilled buzz times).
UPDATE game_sessions
SET buzzers_opened_at = now()
WHERE state IN ('BUZZER_OPEN', 'ANSWER_EVALUATION');
