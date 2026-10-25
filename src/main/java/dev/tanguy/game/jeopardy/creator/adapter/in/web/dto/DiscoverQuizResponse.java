package dev.tanguy.game.jeopardy.creator.adapter.in.web.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * A quiz as anyone may see it before hosting: the board's shape (category names and how many questions), never the
 * questions or answers, so players can't study a board in advance.
 */
public record DiscoverQuizResponse(
        UUID id,
        String name,
        String authorName,
        List<String> topics,
        int timesPlayed,
        Instant publishedAt,
        boolean published,
        /** The requester made it (they can edit it; it may be private). */
        boolean mine,
        int questionCount,
        List<CategoryShape> categories
) {
    public record CategoryShape(String name, int questionCount) {}
}
