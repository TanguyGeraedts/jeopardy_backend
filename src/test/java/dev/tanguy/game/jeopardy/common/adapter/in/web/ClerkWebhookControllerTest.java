package dev.tanguy.game.jeopardy.common.adapter.in.web;

import dev.tanguy.game.jeopardy.common.events.DomainEvent;
import dev.tanguy.game.jeopardy.common.events.DomainEventPublisher;
import dev.tanguy.game.jeopardy.common.events.UserAccountDeletedEvent;
import dev.tanguy.game.jeopardy.common.infrastructure.security.AuthenticatedUser;
import dev.tanguy.game.jeopardy.common.infrastructure.security.ClerkWebhookConfig;
import dev.tanguy.game.jeopardy.common.infrastructure.security.SecurityConfig;
import dev.tanguy.game.jeopardy.common.infrastructure.security.mock.MockJwtConfig;
import dev.tanguy.game.jeopardy.common.web.ApiPaths;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ClerkWebhookController.class)
@Import({SecurityConfig.class, MockJwtConfig.class, ClerkWebhookConfig.class})
@ActiveProfiles("test")
@TestPropertySource(properties = "app.security.clerk-webhook.signing-secret=" + ClerkWebhookControllerTest.SECRET)
class ClerkWebhookControllerTest {

    static final String SECRET = "whsec_dGVzdC13ZWJob29rLWtleS10aGF0LWlzLTMyLWJ5dGU=";
    static final String DELETED = "{\"type\":\"user.deleted\",\"object\":\"event\",\"data\":{\"id\":\"user_2xyz\",\"deleted\":true,\"object\":\"user\"}}";

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    DomainEventPublisher eventPublisher;

    static MockHttpServletRequestBuilder signed(String body) throws Exception {
        String id = "msg_1";
        String timestamp = String.valueOf(Instant.now().getEpochSecond());
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(Base64.getDecoder().decode(SECRET.substring("whsec_".length())), "HmacSHA256"));
        String signature = Base64.getEncoder().encodeToString(
                mac.doFinal((id + "." + timestamp + "." + body).getBytes(StandardCharsets.UTF_8)));
        return post(ApiPaths.Webhooks.CLERK)
                .contentType(MediaType.APPLICATION_JSON)
                .header("svix-id", id)
                .header("svix-timestamp", timestamp)
                .header("svix-signature", "v1," + signature)
                .content(body);
    }

    @Test
    void userDeletedPublishesTheAccountDeletionWithoutALogin() throws Exception {
        mockMvc.perform(signed(DELETED)).andExpect(status().isNoContent());

        ArgumentCaptor<DomainEvent> event = ArgumentCaptor.forClass(DomainEvent.class);
        verify(eventPublisher).publish(event.capture());
        assertThat(event.getValue()).isInstanceOfSatisfying(UserAccountDeletedEvent.class, deleted ->
                // The same owner id the user's quizzes were saved under.
                assertThat(deleted.ownerId()).isEqualTo(AuthenticatedUser.ownerIdOf("user_2xyz")));
    }

    @Test
    void otherEventsAreAcknowledgedAndIgnored() throws Exception {
        mockMvc.perform(signed("{\"type\":\"user.updated\",\"data\":{\"id\":\"user_2xyz\"}}"))
                .andExpect(status().isNoContent());

        verifyNoInteractions(eventPublisher);
    }

    @Test
    void unsignedWebhookIsRejected() throws Exception {
        mockMvc.perform(post(ApiPaths.Webhooks.CLERK).contentType(MediaType.APPLICATION_JSON).content(DELETED))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(eventPublisher);
    }

    @Test
    void forgedSignatureIsRejected() throws Exception {
        mockMvc.perform(signed(DELETED).header("svix-signature", "v1,Zm9yZ2Vk"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(eventPublisher);
    }
}
