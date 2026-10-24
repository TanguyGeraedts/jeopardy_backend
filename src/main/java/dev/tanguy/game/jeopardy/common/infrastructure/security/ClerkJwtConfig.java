package dev.tanguy.game.jeopardy.common.infrastructure.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Host logins are Clerk session tokens (RS256 JWTs), sent by the frontend as "Authorization: Bearer ...".
 * Checked here: signature (Clerk's public keys, fetched from the instance's JWKS on first use), issuer, expiry and
 * "not before", and that the token was issued to one of our frontends ("azp").
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(ClerkProperties.class)
public class ClerkJwtConfig {

    static final String PLACEHOLDER = "YOUR-INSTANCE";

    @Bean
    JwtDecoder jwtDecoder(ClerkProperties clerk) {
        String issuer = clerk.issuer();
        if (!StringUtils.hasText(issuer) || issuer.contains(PLACEHOLDER)) {
            throw new IllegalStateException("""
                    app.security.clerk.issuer is not set. Use your Clerk instance's Frontend API URL \
                    (Clerk dashboard > Configure > API keys), e.g. https://xyz.clerk.accounts.dev; \
                    locally in application.yaml, on a server via the CLERK_ISSUER environment variable.""");
        }
        issuer = issuer.replaceAll("/+$", "");

        // Lazy: the keys are downloaded on the first request, so startup doesn't need Clerk to be reachable.
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(issuer + "/.well-known/jwks.json").build();

        List<OAuth2TokenValidator<Jwt>> validators = new ArrayList<>();
        validators.add(JwtValidators.createDefaultWithIssuer(issuer));
        if (clerk.authorizedParties().isEmpty()) {
            log.warn("app.security.clerk.authorized-parties is empty: tokens issued to any origin are accepted");
        } else {
            validators.add(authorizedParty(clerk.authorizedParties()));
        }
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(validators));

        log.info("Accepting Clerk logins from {} (frontends: {})", issuer, clerk.authorizedParties());
        return decoder;
    }

    /** Clerk puts the frontend origin that asked for the token in "azp". Clerk recommends checking it. */
    private static OAuth2TokenValidator<Jwt> authorizedParty(List<String> allowed) {
        List<String> origins = allowed.stream().map(origin -> origin.replaceAll("/+$", "")).toList();
        return jwt -> {
            String azp = jwt.getClaimAsString("azp");
            if (azp != null && origins.contains(azp)) {
                return OAuth2TokenValidatorResult.success();
            }
            return OAuth2TokenValidatorResult.failure(new OAuth2Error(
                    "invalid_token", "Token was issued to " + azp + ", which is not an allowed frontend", null));
        };
    }
}
