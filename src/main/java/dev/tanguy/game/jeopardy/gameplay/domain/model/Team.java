package dev.tanguy.game.jeopardy.gameplay.domain.model;

import dev.tanguy.game.jeopardy.common.domain.model.id.PlayerId;
import dev.tanguy.game.jeopardy.common.domain.model.id.TeamId;
import lombok.Getter;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Getter
public class Team {
    private final TeamId id;
    private final String name;
    private final Set<PlayerId> memberIds = new HashSet<>();
    private int score;

    public Team(TeamId id, String name) {
        this.id = id;
        this.name = name;
        this.score = 0;
    }

    public void addMember(PlayerId playerId) {
        this.memberIds.add(playerId);
    }

    public void addScore(int points) {
        this.score += points;
    }

    public void subtractScore(int points) {
        this.score -= points;
    }

    public Set<PlayerId> getMemberIds() {
        return Collections.unmodifiableSet(memberIds);
    }
}