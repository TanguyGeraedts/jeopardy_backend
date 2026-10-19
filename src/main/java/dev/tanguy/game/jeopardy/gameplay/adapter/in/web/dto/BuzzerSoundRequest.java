package dev.tanguy.game.jeopardy.gameplay.adapter.in.web.dto;

import dev.tanguy.game.jeopardy.gameplay.domain.model.BuzzerSound;

/** sound null (or left out) = back to the automatic sound for the team's position. */
public record BuzzerSoundRequest(BuzzerSound sound) {}
