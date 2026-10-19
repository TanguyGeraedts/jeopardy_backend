-- The host's block switches are gone: buzzers only open when the host opens them, per clue.
ALTER TABLE game_sessions
    DROP COLUMN buzzers_blocked;

ALTER TABLE game_session_teams
    DROP COLUMN buzzer_blocked;
