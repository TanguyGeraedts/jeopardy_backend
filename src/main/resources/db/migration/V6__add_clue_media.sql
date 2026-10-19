-- Image or video shown with a clue's question (copied from the quiz when the game is created).
ALTER TABLE game_session_clues
    ADD COLUMN media_type VARCHAR(10),
    ADD COLUMN media_url  VARCHAR(2048);
