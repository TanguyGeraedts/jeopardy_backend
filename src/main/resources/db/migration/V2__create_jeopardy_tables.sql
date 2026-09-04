-- Categories for the game board
CREATE TABLE categories (
                            id BIGSERIAL PRIMARY KEY,
                            name VARCHAR(255) NOT NULL,
                            description TEXT
);

-- Individual clues/questions belonging to a category
CREATE TABLE clues (
                       id BIGSERIAL PRIMARY KEY,
                       category_id BIGINT NOT NULL REFERENCES categories(id) ON DELETE CASCADE,
                       question TEXT NOT NULL,
                       answer VARCHAR(255) NOT NULL,
                       value INT NOT NULL,
                       is_daily_double BOOLEAN NOT NULL DEFAULT FALSE
);

-- Active or historical games
CREATE TABLE games (
                       id UUID PRIMARY KEY,
                       status VARCHAR(50) NOT NULL DEFAULT 'CREATED', -- e.g., CREATED, IN_PROGRESS, COMPLETED
                       created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Players participating in games
CREATE TABLE players (
                         id UUID PRIMARY KEY,
                         username VARCHAR(100) NOT NULL UNIQUE,
                         created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Tracks scores and state for a player in a specific game
CREATE TABLE game_players (
                              game_id UUID NOT NULL REFERENCES games(id) ON DELETE CASCADE,
                              player_id UUID NOT NULL REFERENCES players(id) ON DELETE CASCADE,
                              score INT NOT NULL DEFAULT 0,
                              PRIMARY KEY (game_id, player_id)
);