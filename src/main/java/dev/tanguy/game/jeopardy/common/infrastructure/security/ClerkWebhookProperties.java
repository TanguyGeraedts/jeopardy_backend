package dev.tanguy.game.jeopardy.common.infrastructure.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

/**
 * Clerk's webhooks (account deletions), signed by Svix.
 *
 * @param signingSecret the endpoint's signing secret, "whsec_..." (Clerk dashboard > Configure > Webhooks > the
 *                      endpoint). Blank = every webhook is rejected.
 * @param tolerance     how far a webhook's timestamp may be from now; older ones are treated as replays
 */
@ConfigurationProperties("app.security.clerk-webhook")
public record ClerkWebhookProperties(String signingSecret, @DefaultValue("5m") Duration tolerance) {}
