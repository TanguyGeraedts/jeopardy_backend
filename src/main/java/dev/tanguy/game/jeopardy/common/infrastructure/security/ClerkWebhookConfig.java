package dev.tanguy.game.jeopardy.common.infrastructure.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Slf4j
@Configuration
@EnableConfigurationProperties(ClerkWebhookProperties.class)
public class ClerkWebhookConfig {

    @Bean
    ClerkWebhookVerifier clerkWebhookVerifier(ClerkWebhookProperties properties) {
        ClerkWebhookVerifier verifier = new ClerkWebhookVerifier(properties.signingSecret(), properties.tolerance(), Clock.systemUTC());
        if (!verifier.isConfigured()) {
            log.warn("app.security.clerk-webhook.signing-secret is not set: Clerk webhooks are rejected, "
                    + "so deleted accounts keep their quizzes");
        }
        return verifier;
    }
}
