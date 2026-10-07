package dev.tanguy.game.jeopardy.gameplay.adapter.in.web;

import dev.tanguy.game.jeopardy.common.domain.model.id.GameSessionId;
import dev.tanguy.game.jeopardy.common.web.ApiPaths;
import dev.tanguy.game.jeopardy.common.web.ApiResponse;
import dev.tanguy.game.jeopardy.gameplay.adapter.in.web.dto.GameSessionResponse;
import dev.tanguy.game.jeopardy.gameplay.adapter.in.web.dto.LobbyHandoffRequest;
import dev.tanguy.game.jeopardy.gameplay.adapter.in.web.mapper.GameSessionResponseMapper;
import dev.tanguy.game.jeopardy.gameplay.domain.model.GameSession;
import dev.tanguy.game.jeopardy.gameplay.port.in.session.StartGameFromLobbyCommand;
import dev.tanguy.game.jeopardy.gameplay.port.in.session.StartGameFromLobbyUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Receives the lobby's signed handoff token (from whichever player started the lobby) and starts the game.
 * Public on purpose: the token is the credential. See SecurityConfig.
 */
@RestController
@RequestMapping(ApiPaths.Gameplay.BASE)
@RequiredArgsConstructor
public class GameSessionHandoffController {

    private final StartGameFromLobbyUseCase startGameFromLobbyUseCase;

    @PostMapping(ApiPaths.Gameplay.HANDOFF)
    public ResponseEntity<ApiResponse<GameSessionResponse>> handoff(
            @PathVariable UUID id,
            @Valid @RequestBody LobbyHandoffRequest request
    ) {
        GameSession session = startGameFromLobbyUseCase.startFromHandoff(new StartGameFromLobbyCommand(
                new GameSessionId(id),
                request.token()));

        return ResponseEntity.ok(ApiResponse.success(
                GameSessionResponseMapper.toResponse(session), "Game started from lobby"));
    }
}