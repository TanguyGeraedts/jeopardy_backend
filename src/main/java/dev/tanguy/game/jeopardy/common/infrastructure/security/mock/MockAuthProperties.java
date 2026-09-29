package dev.tanguy.game.jeopardy.common.infrastructure.security.mock;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.security.mock")
public record MockAuthProperties(
        @DefaultValue("http://localhost:8080/mock-issuer") String issuer,
        String secret,
        @DefaultValue("1h") Duration ttl
) {
}