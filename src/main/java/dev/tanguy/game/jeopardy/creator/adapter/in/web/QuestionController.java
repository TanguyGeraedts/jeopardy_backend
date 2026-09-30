package dev.tanguy.game.jeopardy.creator.adapter.in.web;

import dev.tanguy.game.jeopardy.common.domain.model.id.OwnerId;
import dev.tanguy.game.jeopardy.common.infrastructure.security.CurrentUser;
import dev.tanguy.game.jeopardy.common.web.ApiPaths;
import dev.tanguy.game.jeopardy.common.web.ApiResponse;
import dev.tanguy.game.jeopardy.creator.adapter.in.web.dto.QuestionRequest;
import dev.tanguy.game.jeopardy.creator.adapter.in.web.dto.QuizResponse.QuestionResponse;
import dev.tanguy.game.jeopardy.creator.adapter.in.web.mapper.QuestionWebMapper;
import dev.tanguy.game.jeopardy.creator.adapter.in.web.mapper.QuizWebMapper;
import dev.tanguy.game.jeopardy.creator.domain.model.Question;
import dev.tanguy.game.jeopardy.creator.port.in.question.AddQuestionUseCase;
import dev.tanguy.game.jeopardy.creator.port.in.question.RemoveQuestionUseCase;
import dev.tanguy.game.jeopardy.creator.port.in.question.UpdateQuestionUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping(ApiPaths.Creator.BASE)
@RequiredArgsConstructor
public class QuestionController {

    /// Auth
    private final CurrentUser currentUser;

    /// Use cases
    private final AddQuestionUseCase addQuestionUseCase;
    private final UpdateQuestionUseCase updateQuestionUseCase;
    private final RemoveQuestionUseCase removeQuestionUseCase;

    @PostMapping(ApiPaths.Creator.QUESTIONS)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<QuestionResponse>> addQuestion(
            @PathVariable UUID id,
            @PathVariable UUID categoryId,
            @Valid @RequestBody QuestionRequest request) {
        OwnerId requesterId = currentUser.require().ownerId();

        Question question = addQuestionUseCase.addQuestion(
                QuestionWebMapper.toAddCommand(id, categoryId, request, requesterId));

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(QuizWebMapper.toQuestionResponse(question), "Question created successfully"));
    }

    @PutMapping(ApiPaths.Creator.QUESTION_BY_ID)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<QuestionResponse>> updateQuestion(
            @PathVariable UUID id,
            @PathVariable UUID categoryId,
            @PathVariable UUID questionId,
            @Valid @RequestBody QuestionRequest request) {
        OwnerId requesterId = currentUser.require().ownerId();

        Question question = updateQuestionUseCase.updateQuestion(
                QuestionWebMapper.toUpdateCommand(id, categoryId, questionId, request, requesterId));

        return ResponseEntity.ok(
                ApiResponse.success(QuizWebMapper.toQuestionResponse(question), "Question updated successfully"));
    }

    @DeleteMapping(ApiPaths.Creator.QUESTION_BY_ID)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> removeQuestion(
            @PathVariable UUID id,
            @PathVariable UUID categoryId,
            @PathVariable UUID questionId) {
        OwnerId requesterId = currentUser.require().ownerId();

        removeQuestionUseCase.removeQuestion(
                QuestionWebMapper.toRemoveCommand(id, categoryId, questionId, requesterId));

        return ResponseEntity.noContent().build();
    }
}