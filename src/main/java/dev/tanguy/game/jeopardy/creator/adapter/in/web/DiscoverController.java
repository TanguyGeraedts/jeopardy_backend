package dev.tanguy.game.jeopardy.creator.adapter.in.web;

import dev.tanguy.game.jeopardy.common.domain.model.id.OwnerId;
import dev.tanguy.game.jeopardy.common.domain.model.id.QuizId;
import dev.tanguy.game.jeopardy.common.infrastructure.security.CurrentUser;
import dev.tanguy.game.jeopardy.common.web.ApiPaths;
import dev.tanguy.game.jeopardy.common.web.ApiResponse;
import dev.tanguy.game.jeopardy.creator.adapter.in.web.dto.DiscoverQuizResponse;
import dev.tanguy.game.jeopardy.creator.adapter.in.web.mapper.QuizWebMapper;
import dev.tanguy.game.jeopardy.creator.domain.model.DiscoverSort;
import dev.tanguy.game.jeopardy.creator.domain.model.QuizTopic;
import dev.tanguy.game.jeopardy.creator.port.in.quiz.DiscoverQuizzesUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/** Finding quizzes to host. Answers never leave through here: only the board's shape. */
@RestController
@RequestMapping(ApiPaths.Discover.BASE)
@RequiredArgsConstructor
public class DiscoverController {

    private final CurrentUser currentUser;
    private final DiscoverQuizzesUseCase discoverQuizzesUseCase;

    /** ?q= matches name or author, ?topic= a QuizTopic name, ?sort=POPULAR (default) or NEWEST. */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<DiscoverQuizResponse>>> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) QuizTopic topic,
            @RequestParam(required = false) DiscoverSort sort) {
        OwnerId requesterId = currentUser.require().ownerId();
        List<DiscoverQuizResponse> results = discoverQuizzesUseCase.search(q, topic, sort).stream()
                .map(quiz -> QuizWebMapper.toDiscoverResponse(quiz, requesterId))
                .toList();
        return ResponseEntity.ok(ApiResponse.success(results));
    }

    /** One quiz you may host (yours, or published). Someone else's private quiz is a 404. */
    @GetMapping(ApiPaths.Discover.BY_ID)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<DiscoverQuizResponse>> get(@PathVariable UUID id) {
        OwnerId requesterId = currentUser.require().ownerId();
        var quiz = discoverQuizzesUseCase.getHostable(QuizId.of(id.toString()), requesterId);
        return ResponseEntity.ok(ApiResponse.success(QuizWebMapper.toDiscoverResponse(quiz, requesterId)));
    }
}
