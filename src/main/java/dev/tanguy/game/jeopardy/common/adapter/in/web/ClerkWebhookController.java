package dev.tanguy.game.jeopardy.common.adapter.in.web;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import dev.tanguy.game.jeopardy.common.events.DomainEventPublisher;
import dev.tanguy.game.jeopardy.common.events.UserAccountDeletedEvent;
import dev.tanguy.game.jeopardy.common.infrastructure.security.AuthenticatedUser;
import dev.tanguy.game.jeopardy.common.infrastructure.security.ClerkWebhookVerifier;
import dev.tanguy.game.jeopardy.common.web.ApiPaths;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;

/**
 * Clerk calls this when something happens to an account. Only user.deleted is acted on: everything the user owns is
 * deleted. Public (under /api/public): the Svix signature is the credential. Any 2xx tells Clerk to stop retrying.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class ClerkWebhookController {

    private static final String USER_DELETED = "user.deleted";

    private final ClerkWebhookVerifier verifier;
    private final ObjectMapper objectMapper;
    private final DomainEventPublisher eventPublisher;

    /** The body is read as a string: the signature covers the exact bytes Clerk sent. */
    @PostMapping(ApiPaths.Webhooks.CLERK)
    public ResponseEntity<Void> receive(
            @RequestHeader(name = "svix-id", required = false) String id,
            @RequestHeader(name = "svix-timestamp", required = false) String timestamp,
            @RequestHeader(name = "svix-signature", required = false) String signature,
            @RequestBody String body) {
        if (!verifier.isValid(id, timestamp, signature, body)) {
            log.warn("Rejected a Clerk webhook with a missing or invalid signature (svix-id {})", id);
            return ResponseEntity.status(401).build();
        }

        ClerkEvent event;
        try {
            event = objectMapper.readValue(body, ClerkEvent.class);
        } catch (JacksonException e) {
            return ResponseEntity.badRequest().build();
        }

        if (USER_DELETED.equals(event.type()) && event.data() != null && StringUtils.hasText(event.data().id())) {
            eventPublisher.publish(new UserAccountDeletedEvent(AuthenticatedUser.ownerIdOf(event.data().id()), Instant.now()));
            log.info("Deleted the data of Clerk user {} (webhook {})", event.data().id(), id);
        }
        return ResponseEntity.noContent().build();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record ClerkEvent(String type, ClerkUser data) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record ClerkUser(String id) {}
}
