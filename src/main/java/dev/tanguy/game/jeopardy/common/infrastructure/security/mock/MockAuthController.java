package dev.tanguy.game.jeopardy.common.infrastructure.security.mock;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** DEV ONLY: does not exist in any other profile. */
@RestController
@Profile("dev")
@RequestMapping("/api/public/mock-auth")
@RequiredArgsConstructor
public class MockAuthController {

    private final MockTokenService tokenService;

    @PostMapping("/token")
    public MockTokenResponse token(@RequestBody(required = false) MockTokenRequest request) {
        MockTokenRequest req = request != null ? request : new MockTokenRequest(null, null, null);
        MockTokenService.IssuedToken issued = tokenService.issue(req.sub(), req.email(), req.roles());
        return new MockTokenResponse(issued.value(), "Bearer", issued.expiresInSeconds());
    }

    public record MockTokenRequest(String sub, String email, List<String> roles) {
    }

    public record MockTokenResponse(
            @JsonProperty("access_token") String accessToken,
            @JsonProperty("token_type") String tokenType,
            @JsonProperty("expires_in") long expiresIn) {
    }
}