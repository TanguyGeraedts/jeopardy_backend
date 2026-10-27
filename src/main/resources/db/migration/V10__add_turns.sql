-- Taking turns: each clue one team gets a head start to buzz, then anyone may steal.
ALTER TABLE game_sessions
    ADD COLUMN turns_enabled      BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN head_start_seconds INT     NOT NULL DEFAULT 7,
    -- Whose turn it is (gets the head start on the next clue).
    ADD COLUMN turn_team_id       UUID,
    -- The team with the head start on the open clue.
    ADD COLUMN clue_turn_team_id  UUID;

-- The lobby's team order: the scoreboard and the turn rotation follow it.
ALTER TABLE game_session_teams
    ADD COLUMN position INT NOT NULL DEFAULT 0;
