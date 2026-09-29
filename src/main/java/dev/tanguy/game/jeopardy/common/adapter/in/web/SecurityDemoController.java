package dev.tanguy.game.jeopardy.common.adapter.in.web;

import dev.tanguy.game.jeopardy.common.infrastructure.security.AuthenticatedUser;
import dev.tanguy.game.jeopardy.common.infrastructure.security.CurrentUser;
import dev.tanguy.game.jeopardy.common.web.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/demo")
@ConditionalOnProperty(name = "app.security.demo-endpoints", havingValue = "true")
@RequiredArgsConstructor
public class SecurityDemoController {

    private final CurrentUser currentUser;

    /** Any authenticated caller. */
    @GetMapping("/me")
    public ApiResponse<MeResponse> me() {
        AuthenticatedUser user = currentUser.require();
        return ApiResponse.success(new MeResponse(user.subject(), user.email(), user.roles(), user.ownerId().value()));
    }

    /** ROLE_ADMIN only. */
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<String> admin() {
        return ApiResponse.success("Hello admin " + currentUser.require().subject());
    }

    public record MeResponse(String subject, String email, List<String> roles, UUID ownerId) {
    }
}