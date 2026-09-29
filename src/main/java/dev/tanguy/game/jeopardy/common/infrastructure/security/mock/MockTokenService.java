package dev.tanguy.game.jeopardy.common.infrastructure.security.mock;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Profile("dev")
@RequiredArgsConstructor
public class MockTokenService {

    /** Stable default so data created with default tokens keeps the same owner. */
    static final String DEFAULT_SUBJECT = "00000000-0000-0000-0000-000000000001";
    static final String DEFAULT_EMAIL = "dev.user@example.com";

    private final JwtEncoder encoder;
    private final MockAuthProperties properties;

    public IssuedToken issue(String subject, String email, List<String> roles) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(properties.ttl());

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(properties.issuer())
                .subject(StringUtils.hasText(subject) ? subject : DEFAULT_SUBJECT)
                .issuedAt(now)
                .expiresAt(expiresAt)
                .id(UUID.randomUUID().toString())
                .claim("email", StringUtils.hasText(email) ? email : DEFAULT_EMAIL)
                .claim("roles", roles == null || roles.isEmpty() ? List.of("USER") : List.copyOf(roles))
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).type("JWT").build();
        String token = encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new IssuedToken(token, properties.ttl().toSeconds());
    }

    public record IssuedToken(String value, long expiresInSeconds) {
    }
}