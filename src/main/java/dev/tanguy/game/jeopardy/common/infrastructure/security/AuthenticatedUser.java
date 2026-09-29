package dev.tanguy.game.jeopardy.common.infrastructure.security;

import dev.tanguy.game.jeopardy.common.domain.model.id.OwnerId;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

public record AuthenticatedUser(String subject, String email, List<String> roles) {

    public OwnerId ownerId() {
        try {
            return new OwnerId(UUID.fromString(subject));
        } catch (IllegalArgumentException e) {
            return new OwnerId(UUID.nameUUIDFromBytes(subject.getBytes(StandardCharsets.UTF_8)));
        }
    }
}