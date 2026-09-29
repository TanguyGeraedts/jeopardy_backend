package dev.tanguy.game.jeopardy.common.infrastructure.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class ClaimRolesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private final List<String> claimPaths;
    private final String prefix;
    private final boolean uppercase;

    public ClaimRolesConverter(JwtRoleMappingProperties properties) {
        this.claimPaths = List.copyOf(properties.rolesClaimPaths());
        this.prefix = properties.authorityPrefix();
        this.uppercase = properties.uppercase();
    }

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        Set<GrantedAuthority> authorities = new LinkedHashSet<>();
        for (String path : claimPaths) {
            for (String raw : asStrings(resolve(jwt.getClaims(), path))) {
                String role = normalize(raw);
                if (!role.isEmpty()) {
                    authorities.add(new SimpleGrantedAuthority(role));
                }
            }
        }
        return authorities;
    }

    private static Object resolve(Map<String, Object> claims, String path) {
        if (claims.containsKey(path)) {
            return claims.get(path);
        }
        Object current = claims;
        for (String segment : path.split("\\.")) {
            if (!(current instanceof Map<?, ?> map)) {
                return null;
            }
            current = map.get(segment);
            if (current == null) {
                return null;
            }
        }
        return current;
    }

    private static List<String> asStrings(Object value) {
        if (value instanceof Collection<?> collection) {
            return collection.stream().filter(Objects::nonNull).map(Object::toString).toList();
        }
        if (value instanceof Map<?, ?> map) {
            return map.keySet().stream().map(Object::toString).toList();
        }
        if (value instanceof String string) {
            return List.of(string.split("[\\s,]+"));
        }
        return List.of();
    }

    private String normalize(String raw) {
        String role = raw.trim();
        if (role.isEmpty()) {
            return "";
        }
        if (uppercase) {
            role = role.toUpperCase(Locale.ROOT);
        }
        return role.startsWith(prefix) ? role : prefix + role;
    }
}