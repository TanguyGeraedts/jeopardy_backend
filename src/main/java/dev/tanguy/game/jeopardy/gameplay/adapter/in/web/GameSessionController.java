package dev.tanguy.game.jeopardy.gameplay.adapter.in.web;

import dev.tanguy.game.jeopardy.common.domain.model.id.ClueId;
import dev.tanguy.game.jeopardy.common.domain.model.id.GameSessionId;
import dev.tanguy.game.jeopardy.common.domain.model.id.PlayerId;
import dev.tanguy.game.jeopardy.common.web.ApiPaths;
import dev.tanguy.game.jeopardy.common.web.ApiResponse;
import dev.tanguy.game.jeopardy.gameplay.adapter.in.web.dto.AnswerClueRequest;
import dev.tanguy.game.jeopardy.gameplay.adapter.in.web.dto.AnswerClueResponse;
import dev.tanguy.game.jeopardy.gameplay.adapter.in.web.dto.CreateGameSessionRequest;
import dev.tanguy.game.jeopardy.gameplay.adapter.in.web.dto.CreateGameSessionResponse;
import dev.tanguy.game.jeopardy.gameplay.adapter.in.web.dto.GameSessionResponse;
import dev.tanguy.game.jeopardy.gameplay.adapter.in.web.dto.JoinGameSessionRequest;
import dev.tanguy.game.jeopardy.gameplay.adapter.in.web.dto.JoinGameSessionResponse;
import dev.tanguy.game.jeopardy.gameplay.adapter.in.web.mapper.GameSessionResponseMapper;
import dev.tanguy.game.jeopardy.gameplay.domain.model.GameMode;
import dev.tanguy.game.jeopardy.gameplay.domain.model.GameSession;
import dev.tanguy.game.jeopardy.gameplay.port.in.session.AddPlayerCommand;
import dev.tanguy.game.jeopardy.gameplay.port.in.session.AddPlayerResult;
import dev.tanguy.game.jeopardy.gameplay.port.in.session.AddPlayerUseCase;
import dev.tanguy.game.jeopardy.gameplay.port.in.session.AnswerClueCommand;
import dev.tanguy.game.jeopardy.gameplay.port.in.session.AnswerClueResult;
import dev.tanguy.game.jeopardy.gameplay.port.in.session.AnswerClueUseCase;
import dev.tanguy.game.jeopardy.gameplay.port.in.session.CreateGameSessionCommand;
import dev.tanguy.game.jeopardy.gameplay.port.in.session.CreateGameSessionUseCase;
import dev.tanguy.game.jeopardy.gameplay.port.in.session.GetGameSessionQuery;
import dev.tanguy.game.jeopardy.gameplay.port.in.session.GetGameSessionUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping(ApiPaths.Gameplay.BASE)
@RequiredArgsConstructor
public class GameSessionController {

    private final CreateGameSessionUseCase createGameSessionUseCase;
    private final GetGameSessionUseCase getGameSessionUseCase;
    private final AnswerClueUseCase answerClueUseCase;
    private final AddPlayerUseCase addPlayerUseCase;

    @PostMapping
    public ResponseEntity<ApiResponse<CreateGameSessionResponse>> createGameSession(
            @Valid @RequestBody CreateGameSessionRequest request
    ) {
        CreateGameSessionCommand command = new CreateGameSessionCommand(
                request.boardId(),
                request.teamGame() ? GameMode.TEAM : GameMode.SOLO
        );
        GameSessionId sessionId = createGameSessionUseCase.createGameSession(command);

        CreateGameSessionResponse responseData = new CreateGameSessionResponse(sessionId.value());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(responseData, "Game session created successfully"));
    }

    @GetMapping(ApiPaths.Gameplay.BY_ID)
    public ResponseEntity<ApiResponse<GameSessionResponse>> getGameSession(@PathVariable UUID id) {
        GameSession session = getGameSessionUseCase.getGameSession(new GetGameSessionQuery(new GameSessionId(id)));

        return ResponseEntity.ok(ApiResponse.success(GameSessionResponseMapper.toResponse(session)));
    }

    @PostMapping(ApiPaths.Gameplay.JOIN)
    public ResponseEntity<ApiResponse<JoinGameSessionResponse>> joinGameSession(
            @PathVariable UUID id,
            @Valid @RequestBody JoinGameSessionRequest request
    ) {
        AddPlayerCommand command = new AddPlayerCommand(
                new GameSessionId(id),
                new PlayerId(request.playerId()),
                request.playerName(),
                request.externalTeamId(),
                request.teamName(),
                request.teamColour()
        );

        AddPlayerResult result = addPlayerUseCase.addPlayer(command);

        JoinGameSessionResponse responseData = new JoinGameSessionResponse(
                result.playerId().value(), result.teamId().value(), result.alreadyJoined()
        );

        HttpStatus status = result.alreadyJoined() ? HttpStatus.OK : HttpStatus.CREATED;
        String message = result.alreadyJoined() ? "Already joined" : "Joined game session successfully";

        return ResponseEntity.status(status).body(ApiResponse.success(responseData, message));
    }

    @PostMapping(ApiPaths.Gameplay.ANSWER)
    public ResponseEntity<ApiResponse<AnswerClueResponse>> answerClue(
            @PathVariable UUID id,
            @Valid @RequestBody AnswerClueRequest request
    ) {
        AnswerClueCommand command = new AnswerClueCommand(
                new GameSessionId(id),
                ClueId.of(request.clueId()),
                request.answer()
        );

        AnswerClueResult result = answerClueUseCase.answerClue(command);

        AnswerClueResponse responseData = new AnswerClueResponse(
                result.correct(), result.scoreDelta(), result.expectedAnswer()
        );

        return ResponseEntity.ok(ApiResponse.success(responseData, "Answer evaluated"));
    }
}