package dev.tanguy.game.jeopardy.common.infrastructure.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.List;

@ConfigurationProperties(prefix = "app.security.jwt")
public record JwtRoleMappingProperties(
        @DefaultValue({"roles", "realm_access.roles"}) List<String> rolesClaimPaths,
        @DefaultValue("ROLE_") String authorityPrefix,
        @DefaultValue("true") boolean uppercase
) {
}