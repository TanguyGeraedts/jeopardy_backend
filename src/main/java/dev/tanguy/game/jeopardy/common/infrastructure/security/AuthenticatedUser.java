package dev.tanguy.game.jeopardy.common.infrastructure.security;

import dev.tanguy.game.jeopardy.common.domain.model.id.OwnerId;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

public record AuthenticatedUser(String subject, String email, List<String> roles) {

    public OwnerId ownerId() {
        return ownerIdOf(subject);
    }

    /** The owner id for a login subject. Clerk ids ("user_...") aren't UUIDs, so they map to a stable name-based one. */
    public static OwnerId ownerIdOf(String subject) {
        try {
            return new OwnerId(UUID.fromString(subject));
        } catch (IllegalArgumentException e) {
            return new OwnerId(UUID.nameUUIDFromBytes(subject.getBytes(StandardCharsets.UTF_8)));
        }
    }
}