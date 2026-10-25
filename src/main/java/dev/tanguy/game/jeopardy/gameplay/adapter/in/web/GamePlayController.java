package dev.tanguy.game.jeopardy.gameplay.adapter.in.web;

import dev.tanguy.game.jeopardy.common.domain.model.id.ClueId;
import dev.tanguy.game.jeopardy.common.domain.model.id.GameSessionId;
import dev.tanguy.game.jeopardy.common.domain.model.id.OwnerId;
import dev.tanguy.game.jeopardy.common.domain.model.id.TeamId;
import dev.tanguy.game.jeopardy.common.infrastructure.security.CurrentUser;
import dev.tanguy.game.jeopardy.common.web.ApiPaths;
import dev.tanguy.game.jeopardy.common.web.ApiResponse;
import dev.tanguy.game.jeopardy.gameplay.adapter.in.web.dto.BuzzResponse;
import dev.tanguy.game.jeopardy.gameplay.adapter.in.web.dto.BuzzerSoundRequest;
import dev.tanguy.game.jeopardy.gameplay.adapter.in.web.dto.ChangeScoreRequest;
import dev.tanguy.game.jeopardy.gameplay.adapter.in.web.dto.GameSessionResponse;
import dev.tanguy.game.jeopardy.gameplay.adapter.in.web.dto.JudgeAnswerRequest;
import dev.tanguy.game.jeopardy.gameplay.adapter.in.web.live.GameSessionLiveUpdates;
import dev.tanguy.game.jeopardy.gameplay.adapter.in.web.mapper.GameSessionResponseMapper;
import dev.tanguy.game.jeopardy.gameplay.domain.model.GameSession;
import dev.tanguy.game.jeopardy.gameplay.port.in.session.BuzzCommand;
import dev.tanguy.game.jeopardy.gameplay.port.in.session.BuzzUseCase;
import dev.tanguy.game.jeopardy.gameplay.port.in.session.GetGameSessionUseCase;
import dev.tanguy.game.jeopardy.gameplay.port.in.session.HostControlUseCase;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

/**
 * Running a game: the host's controls (owner only), the players' buzz, and the live stream every screen listens to.
 * Host endpoints return the full session (with answers); everyone also gets the change over the stream.
 */
@RestController
@RequestMapping(ApiPaths.Gameplay.BASE)
@RequiredArgsConstructor
public class GamePlayController {

    /** The lobby-signed player token. Not "Authorization: Bearer": Spring would try to read it as a login. */
    public static final String PLAYER_TOKEN_HEADER = "X-Player-Token";

    private final CurrentUser currentUser;
    private final HostControlUseCase hostControlUseCase;
    private final BuzzUseCase buzzUseCase;
    private final GetGameSessionUseCase getGameSessionUseCase;
    private final GameSessionLiveUpdates liveUpdates;

    // ---------- everyone ----------

    /** Public on purpose: phones have no login. The stream never contains answers. */
    @GetMapping(value = ApiPaths.Gameplay.EVENTS, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter events(@PathVariable UUID id, HttpServletResponse response) {
        // Unchecked load is fine here: liveUpdates only ever sends the public, answer-free view.
        GameSession session = getGameSessionUseCase.getGameSessionForLiveView(new GameSessionId(id));
        // no-transform: proxies (incl. the Next.js /api rewrite) must not gzip/buffer the stream.
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache, no-transform");
        response.setHeader("X-Accel-Buffering", "no");
        return liveUpdates.subscribe(session);
    }

    @PostMapping(ApiPaths.Gameplay.BUZZ)
    public ResponseEntity<ApiResponse<BuzzResponse>> buzz(
            @PathVariable UUID id,
            @RequestHeader(name = PLAYER_TOKEN_HEADER, required = false) String playerToken
    ) {
        boolean accepted = buzzUseCase.buzz(new BuzzCommand(new GameSessionId(id), playerToken));
        return ResponseEntity.ok(ApiResponse.success(new BuzzResponse(accepted), accepted ? "Buzzed in" : "Buzz ignored"));
    }

    // ---------- host (session owner) ----------

    @PostMapping(ApiPaths.Gameplay.SELECT_CLUE)
    public ResponseEntity<ApiResponse<GameSessionResponse>> selectClue(@PathVariable UUID id, @PathVariable String clueId) {
        return ok(hostControlUseCase.selectClue(sessionId(id), owner(), ClueId.of(clueId)));
    }

    @PostMapping(ApiPaths.Gameplay.OPEN_BUZZERS)
    public ResponseEntity<ApiResponse<GameSessionResponse>> openBuzzers(@PathVariable UUID id) {
        return ok(hostControlUseCase.openBuzzers(sessionId(id), owner()));
    }

    @PostMapping(ApiPaths.Gameplay.JUDGE)
    public ResponseEntity<ApiResponse<GameSessionResponse>> judge(
            @PathVariable UUID id, @Valid @RequestBody JudgeAnswerRequest request) {
        return ok(hostControlUseCase.judgeAnswer(sessionId(id), owner(), request.correct()));
    }

    @PostMapping(ApiPaths.Gameplay.CLOSE_CLUE)
    public ResponseEntity<ApiResponse<GameSessionResponse>> closeClue(@PathVariable UUID id) {
        return ok(hostControlUseCase.closeClue(sessionId(id), owner()));
    }

    @PostMapping(ApiPaths.Gameplay.END_GAME)
    public ResponseEntity<ApiResponse<GameSessionResponse>> endGame(@PathVariable UUID id) {
        return ok(hostControlUseCase.endGame(sessionId(id), owner()));
    }

    @PostMapping(ApiPaths.Gameplay.TEAM_SCORE)
    public ResponseEntity<ApiResponse<GameSessionResponse>> changeScore(
            @PathVariable UUID id, @PathVariable UUID teamId, @RequestBody ChangeScoreRequest request) {
        return ok(hostControlUseCase.changeScore(sessionId(id), owner(), new TeamId(teamId), request.delta(), request.score()));
    }

    @PutMapping(ApiPaths.Gameplay.TEAM_BUZZER_SOUND)
    public ResponseEntity<ApiResponse<GameSessionResponse>> setTeamBuzzerSound(
            @PathVariable UUID id, @PathVariable UUID teamId, @RequestBody BuzzerSoundRequest request) {
        return ok(hostControlUseCase.setTeamBuzzerSound(sessionId(id), owner(), new TeamId(teamId), request.sound()));
    }

    private OwnerId owner() {
        return currentUser.require().ownerId();
    }

    private static GameSessionId sessionId(UUID id) {
        return new GameSessionId(id);
    }

    private static ResponseEntity<ApiResponse<GameSessionResponse>> ok(GameSession session) {
        return ResponseEntity.ok(ApiResponse.success(GameSessionResponseMapper.toResponse(session)));
    }
}
