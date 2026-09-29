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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPaths.Creator.BASE)
@RequiredArgsConstructor
public class QuizController {

    private final CreateQuizUseCase createQuizUseCase;
    private final CurrentUser currentUser;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<QuizResponse>> createQuiz(@Valid @RequestBody CreateQuizRequest request) {
        OwnerId ownerId = currentUser.require().ownerId();

        Quiz quiz = createQuizUseCase.createQuiz(QuizWebMapper.toCommand(request, ownerId));

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(QuizWebMapper.toResponse(quiz), "Quiz created successfully"));
    }
}