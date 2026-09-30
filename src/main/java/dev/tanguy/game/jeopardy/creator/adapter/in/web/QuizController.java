package dev.tanguy.game.jeopardy.creator.adapter.in.web;

import dev.tanguy.game.jeopardy.common.domain.model.id.OwnerId;
import dev.tanguy.game.jeopardy.common.infrastructure.security.CurrentUser;
import dev.tanguy.game.jeopardy.common.web.ApiPaths;
import dev.tanguy.game.jeopardy.common.web.ApiResponse;
import dev.tanguy.game.jeopardy.creator.adapter.in.web.dto.CreateQuizRequest;
import dev.tanguy.game.jeopardy.creator.adapter.in.web.dto.QuizResponse;
import dev.tanguy.game.jeopardy.creator.adapter.in.web.mapper.QuizWebMapper;
import dev.tanguy.game.jeopardy.creator.domain.model.Quiz;
import dev.tanguy.game.jeopardy.creator.port.in.quiz.CreateQuizUseCase;
import dev.tanguy.game.jeopardy.creator.port.in.quiz.GetQuizUseCase;
import dev.tanguy.game.jeopardy.creator.port.in.quiz.GetQuizzesByOwnerQuery;
import dev.tanguy.game.jeopardy.creator.port.in.quiz.GetQuizzesByOwnerUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(ApiPaths.Creator.BASE)
@RequiredArgsConstructor
public class QuizController {

    /// Auth
    private final CurrentUser currentUser;

    /// Use cases
    private final CreateQuizUseCase createQuizUseCase;
    private final GetQuizUseCase getQuizUseCase;
    private final GetQuizzesByOwnerUseCase getQuizzesByOwnerUseCase;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<QuizResponse>> createQuiz(@Valid @RequestBody CreateQuizRequest request) {
        OwnerId ownerId = currentUser.require().ownerId();

        Quiz quiz = createQuizUseCase.createQuiz(QuizWebMapper.toCommand(request, ownerId));

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(QuizWebMapper.toResponse(quiz), "Quiz created successfully"));
    }

    @GetMapping(ApiPaths.Creator.BY_ID)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<QuizResponse>> getQuiz(@PathVariable UUID id) {
        OwnerId requesterId = currentUser.require().ownerId();

        Quiz quiz = getQuizUseCase.getQuiz(QuizWebMapper.toQuery(id, requesterId));

        return ResponseEntity.ok(ApiResponse.success(QuizWebMapper.toResponse(quiz)));
    }

    @GetMapping(ApiPaths.Creator.ME)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<QuizResponse>>> getAllMyQuizzes() {
        OwnerId ownerId = currentUser.require().ownerId();

    List<Quiz> quizzes = getQuizzesByOwnerUseCase.getMyQuizzes(QuizWebMapper.toQuery(ownerId));

        List<QuizResponse> responses = quizzes.stream()
                .map(QuizWebMapper::toResponse)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(responses));
    }
}