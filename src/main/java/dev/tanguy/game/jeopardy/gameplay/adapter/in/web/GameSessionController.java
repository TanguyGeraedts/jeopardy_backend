package dev.tanguy.game.jeopardy.gameplay.adapter.in.web;

import dev.tanguy.game.jeopardy.common.domain.model.id.GameSessionId;
import dev.tanguy.game.jeopardy.common.web.ApiPaths;
import dev.tanguy.game.jeopardy.common.web.ApiResponse;
import dev.tanguy.game.jeopardy.gameplay.adapter.in.web.dto.CreateGameSessionRequest;
import dev.tanguy.game.jeopardy.gameplay.adapter.in.web.dto.CreateGameSessionResponse;
import dev.tanguy.game.jeopardy.gameplay.port.in.session.CreateGameSessionCommand;
import dev.tanguy.game.jeopardy.gameplay.port.in.session.CreateGameSessionUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPaths.Gameplay.BASE)
@RequiredArgsConstructor
public class GameSessionController {

    private final CreateGameSessionUseCase createGameSessionUseCase;

    @PostMapping
    public ResponseEntity<ApiResponse<CreateGameSessionResponse>> createGameSession(
            @Valid @RequestBody CreateGameSessionRequest request
    ) {
        CreateGameSessionCommand command = new CreateGameSessionCommand(request.boardId());
        GameSessionId sessionId = createGameSessionUseCase.createGameSession(command);

        CreateGameSessionResponse responseData = new CreateGameSessionResponse(sessionId.value());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(responseData, "Game session created successfully"));
    }
}