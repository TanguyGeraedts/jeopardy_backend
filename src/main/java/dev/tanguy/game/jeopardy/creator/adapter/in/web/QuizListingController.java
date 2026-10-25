package dev.tanguy.game.jeopardy.creator.adapter.in.web;

import dev.tanguy.game.jeopardy.common.domain.model.id.OwnerId;
import dev.tanguy.game.jeopardy.common.domain.model.id.QuizId;
import dev.tanguy.game.jeopardy.common.infrastructure.security.CurrentUser;
import dev.tanguy.game.jeopardy.common.web.ApiPaths;
import dev.tanguy.game.jeopardy.common.web.ApiResponse;
import dev.tanguy.game.jeopardy.creator.adapter.in.web.dto.PublicationRequest;
import dev.tanguy.game.jeopardy.creator.adapter.in.web.dto.QuizResponse;
import dev.tanguy.game.jeopardy.creator.adapter.in.web.dto.TopicsRequest;
import dev.tanguy.game.jeopardy.creator.adapter.in.web.mapper.QuizWebMapper;
import dev.tanguy.game.jeopardy.creator.domain.model.Quiz;
import dev.tanguy.game.jeopardy.creator.port.in.quiz.ManageQuizListingUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;
import java.util.UUID;

/** The owner's controls for how a quiz is listed: public/private, topics, and copies. */
@RestController
@RequestMapping(ApiPaths.Creator.BASE)
@RequiredArgsConstructor
public class QuizListingController {

    private final CurrentUser currentUser;
    private final ManageQuizListingUseCase manageQuizListingUseCase;

    @PutMapping(ApiPaths.Creator.PUBLICATION)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<QuizResponse>> setPublication(
            @PathVariable UUID id, @Valid @RequestBody PublicationRequest request) {
        Quiz quiz = manageQuizListingUseCase.setPublished(quizId(id), owner(), request.published(), request.authorName());
        return ResponseEntity.ok(ApiResponse.success(QuizWebMapper.toResponse(quiz), quiz.isPublished() ? "Quiz published" : "Quiz is private"));
    }

    @PutMapping(ApiPaths.Creator.TOPICS)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<QuizResponse>> changeTopics(
            @PathVariable UUID id, @Valid @RequestBody TopicsRequest request) {
        Quiz quiz = manageQuizListingUseCase.changeTopics(quizId(id), owner(), new HashSet<>(request.topics()));
        return ResponseEntity.ok(ApiResponse.success(QuizWebMapper.toResponse(quiz)));
    }

    @PostMapping(ApiPaths.Creator.DUPLICATE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<QuizResponse>> duplicate(@PathVariable UUID id) {
        Quiz copy = manageQuizListingUseCase.duplicate(quizId(id), owner());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(QuizWebMapper.toResponse(copy), "Quiz copied"));
    }

    private OwnerId owner() {
        return currentUser.require().ownerId();
    }

    private static QuizId quizId(UUID id) {
        return QuizId.of(id.toString());
    }
}
