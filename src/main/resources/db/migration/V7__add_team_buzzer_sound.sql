-- The buzzer sound the host picked for a team. Null = automatic (by team position).
ALTER TABLE game_session_teams
    ADD COLUMN buzzer_sound VARCHAR(20);
