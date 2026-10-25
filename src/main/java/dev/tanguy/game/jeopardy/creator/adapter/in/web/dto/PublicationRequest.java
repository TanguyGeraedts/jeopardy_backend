package dev.tanguy.game.jeopardy.creator.adapter.in.web.dto;

import jakarta.validation.constraints.Size;

/** authorName: the creator's display name, required when publishing (shown as "by …"). */
public record PublicationRequest(
        boolean published,
        @Size(max = 60) String authorName
) {}
