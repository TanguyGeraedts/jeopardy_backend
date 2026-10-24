package dev.tanguy.game.jeopardy.common.infrastructure.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Where host logins come from: a Clerk instance.
 *
 * @param issuer            the instance's Frontend API URL, e.g. https://xyz.clerk.accounts.dev (Development instance)
 *                          or https://clerk.yourdomain.com (Production). Clerk dashboard: Configure > API keys.
 * @param authorizedParties the frontend origins allowed to use tokens here (Clerk's "azp" claim), e.g.
 *                          http://localhost:3000. Empty = don't check (not recommended).
 */
@ConfigurationProperties("app.security.clerk")
public record ClerkProperties(String issuer, List<String> authorizedParties) {

    public ClerkProperties {
        authorizedParties = authorizedParties == null ? List.of() : List.copyOf(authorizedParties);
    }
}
