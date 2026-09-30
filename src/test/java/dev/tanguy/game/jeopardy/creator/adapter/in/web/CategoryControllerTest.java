package dev.tanguy.game.jeopardy.creator.adapter.in.web;

import com.jayway.jsonpath.JsonPath;
import dev.tanguy.game.jeopardy.common.domain.model.id.CategoryId;
import dev.tanguy.game.jeopardy.common.domain.model.id.QuizId;
import dev.tanguy.game.jeopardy.common.infrastructure.security.CurrentUser;
import dev.tanguy.game.jeopardy.common.infrastructure.security.SecurityConfig;
import dev.tanguy.game.jeopardy.common.infrastructure.security.mock.MockAuthController;
import dev.tanguy.game.jeopardy.common.infrastructure.security.mock.MockJwtConfig;
import dev.tanguy.game.jeopardy.common.infrastructure.security.mock.MockTokenService;
import dev.tanguy.game.jeopardy.common.web.ApiPaths;
import dev.tanguy.game.jeopardy.creator.domain.event.quiz.CategoryNotFoundException;
import dev.tanguy.game.jeopardy.creator.domain.event.quiz.DuplicateCategoryNameException;
import dev.tanguy.game.jeopardy.creator.domain.event.quiz.QuizNotFoundException;
import dev.tanguy.game.jeopardy.creator.domain.model.Category;
import dev.tanguy.game.jeopardy.creator.port.in.category.AddCategoryCommand;
import dev.tanguy.game.jeopardy.creator.port.in.category.AddCategoryUseCase;
import dev.tanguy.game.jeopardy.creator.port.in.category.RemoveCategoryCommand;
import dev.tanguy.game.jeopardy.creator.port.in.category.RemoveCategoryUseCase;
import dev.tanguy.game.jeopardy.creator.port.in.category.RenameCategoryCommand;
import dev.tanguy.game.jeopardy.creator.port.in.category.RenameCategoryUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {CategoryController.class, MockAuthController.class})
@Import({SecurityConfig.class, MockJwtConfig.class, MockTokenService.class, CurrentUser.class})
@ActiveProfiles("dev")
class CategoryControllerTest {

    private static final String ALICE = "11111111-1111-1111-1111-111111111111";

    private final UUID quizId = UUID.randomUUID();
    private final UUID categoryId = UUID.randomUUID();

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    AddCategoryUseCase addCategoryUseCase;

    @MockitoBean
    RenameCategoryUseCase renameCategoryUseCase;

    @MockitoBean
    RemoveCategoryUseCase removeCategoryUseCase;

    @BeforeEach
    void stubUseCases() {
        given(addCategoryUseCase.addCategory(any())).willAnswer(invocation -> {
            AddCategoryCommand command = invocation.getArgument(0);
            return new Category(CategoryId.generate(), command.quizId(), command.name());
        });

        given(renameCategoryUseCase.renameCategory(any())).willAnswer(invocation -> {
            RenameCategoryCommand command = invocation.getArgument(0);
            return new Category(command.categoryId(), command.quizId(), command.name());
        });
    }

    private String categories() {
        return ApiPaths.Creator.BASE + "/" + quizId + "/categories";
    }

    private String category() {
        return categories() + "/" + categoryId;
    }

    // ---------- add (POST /quizzes/{id}/categories) ----------

    @Test
    void addCategory_withoutToken_isUnauthorized() throws Exception {
        mockMvc.perform(post(categories()).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Science\"}"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(addCategoryUseCase);
    }

    @Test
    void addCategory_returnsCreated_andPassesQuizAndCallerToTheUseCase() throws Exception {
        mockMvc.perform(post(categories())
                        .header("Authorization", "Bearer " + tokenFor(ALICE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"  Science  \",\"ownerId\":\"99999999-9999-9999-9999-999999999999\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.name").value("Science"))
                .andExpect(jsonPath("$.data.questions").isEmpty());

        ArgumentCaptor<AddCategoryCommand> captor = ArgumentCaptor.forClass(AddCategoryCommand.class);
        verify(addCategoryUseCase).addCategory(captor.capture());
        assertThat(captor.getValue().quizId().value()).isEqualTo(quizId.toString());
        assertThat(captor.getValue().requesterId().value()).isEqualTo(UUID.fromString(ALICE));
        assertThat(captor.getValue().name()).isEqualTo("Science");
    }

    @Test
    void addCategory_withBlankName_isBadRequest() throws Exception {
        mockMvc.perform(post(categories())
                        .header("Authorization", "Bearer " + tokenFor(ALICE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Failed"))
                .andExpect(jsonPath("$.errors.name").exists());

        verifyNoInteractions(addCategoryUseCase);
    }

    @Test
    void addCategory_withMissingName_isBadRequest() throws Exception {
        mockMvc.perform(post(categories())
                        .header("Authorization", "Bearer " + tokenFor(ALICE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(addCategoryUseCase);
    }

    @Test
    void addCategory_withTooLongName_isBadRequest() throws Exception {
        mockMvc.perform(post(categories())
                        .header("Authorization", "Bearer " + tokenFor(ALICE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + "a".repeat(256) + "\"}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(addCategoryUseCase);
    }

    @Test
    void addCategory_withMalformedJson_isBadRequest() throws Exception {
        mockMvc.perform(post(categories())
                        .header("Authorization", "Bearer " + tokenFor(ALICE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{bad"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(addCategoryUseCase);
    }

    @Test
    void addCategory_withNonUuidQuizId_isBadRequest() throws Exception {
        mockMvc.perform(post(ApiPaths.Creator.BASE + "/not-a-uuid/categories")
                        .header("Authorization", "Bearer " + tokenFor(ALICE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Science\"}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(addCategoryUseCase);
    }

    @Test
    void addCategory_whenQuizIsNotFoundOrNotOwned_isNotFound() throws Exception {
        willThrow(new QuizNotFoundException(QuizId.generate())).given(addCategoryUseCase).addCategory(any());

        mockMvc.perform(post(categories())
                        .header("Authorization", "Bearer " + tokenFor(ALICE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Science\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource Not Found"));
    }

    @Test
    void addCategory_whenNameAlreadyExists_isConflict() throws Exception {
        willThrow(new DuplicateCategoryNameException(QuizId.generate(), "Science"))
                .given(addCategoryUseCase).addCategory(any());

        mockMvc.perform(post(categories())
                        .header("Authorization", "Bearer " + tokenFor(ALICE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Science\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Domain Invariant Conflict"));
    }

    // ---------- rename (PUT /quizzes/{id}/categories/{categoryId}) ----------

    @Test
    void renameCategory_withoutToken_isUnauthorized() throws Exception {
        mockMvc.perform(put(category()).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"History\"}"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(renameCategoryUseCase);
    }

    @Test
    void renameCategory_returnsUpdatedCategory_andPassesAllIdsAndCaller() throws Exception {
        mockMvc.perform(put(category())
                        .header("Authorization", "Bearer " + tokenFor(ALICE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"  History  \"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(categoryId.toString()))
                .andExpect(jsonPath("$.data.name").value("History"));

        ArgumentCaptor<RenameCategoryCommand> captor = ArgumentCaptor.forClass(RenameCategoryCommand.class);
        verify(renameCategoryUseCase).renameCategory(captor.capture());
        assertThat(captor.getValue().quizId().value()).isEqualTo(quizId.toString());
        assertThat(captor.getValue().categoryId().value()).isEqualTo(categoryId.toString());
        assertThat(captor.getValue().requesterId().value()).isEqualTo(UUID.fromString(ALICE));
        assertThat(captor.getValue().name()).isEqualTo("History");
    }

    @Test
    void renameCategory_withBlankName_isBadRequest() throws Exception {
        mockMvc.perform(put(category())
                        .header("Authorization", "Bearer " + tokenFor(ALICE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists());

        verifyNoInteractions(renameCategoryUseCase);
    }

    @Test
    void renameCategory_withNonUuidCategoryId_isBadRequest() throws Exception {
        mockMvc.perform(put(categories() + "/not-a-uuid")
                        .header("Authorization", "Bearer " + tokenFor(ALICE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"History\"}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(renameCategoryUseCase);
    }

    @Test
    void renameCategory_whenCategoryDoesNotExist_isNotFound() throws Exception {
        willThrow(new CategoryNotFoundException(QuizId.generate(), CategoryId.generate()))
                .given(renameCategoryUseCase).renameCategory(any());

        mockMvc.perform(put(category())
                        .header("Authorization", "Bearer " + tokenFor(ALICE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"History\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource Not Found"));
    }

    @Test
    void renameCategory_whenNameAlreadyExists_isConflict() throws Exception {
        willThrow(new DuplicateCategoryNameException(QuizId.generate(), "History"))
                .given(renameCategoryUseCase).renameCategory(any());

        mockMvc.perform(put(category())
                        .header("Authorization", "Bearer " + tokenFor(ALICE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"History\"}"))
                .andExpect(status().isConflict());
    }

    // ---------- remove (DELETE /quizzes/{id}/categories/{categoryId}) ----------

    @Test
    void removeCategory_withoutToken_isUnauthorized() throws Exception {
        mockMvc.perform(delete(category()))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(removeCategoryUseCase);
    }

    @Test
    void removeCategory_returnsNoContent_andPassesAllIdsAndCaller() throws Exception {
        mockMvc.perform(delete(category())
                        .header("Authorization", "Bearer " + tokenFor(ALICE)))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        ArgumentCaptor<RemoveCategoryCommand> captor = ArgumentCaptor.forClass(RemoveCategoryCommand.class);
        verify(removeCategoryUseCase).removeCategory(captor.capture());
        assertThat(captor.getValue().quizId().value()).isEqualTo(quizId.toString());
        assertThat(captor.getValue().categoryId().value()).isEqualTo(categoryId.toString());
        assertThat(captor.getValue().requesterId().value()).isEqualTo(UUID.fromString(ALICE));
    }

    @Test
    void removeCategory_whenCategoryDoesNotExist_isNotFound() throws Exception {
        willThrow(new CategoryNotFoundException(QuizId.generate(), CategoryId.generate()))
                .given(removeCategoryUseCase).removeCategory(any());

        mockMvc.perform(delete(category())
                        .header("Authorization", "Bearer " + tokenFor(ALICE)))
                .andExpect(status().isNotFound());
    }

    @Test
    void removeCategory_whenQuizIsNotFoundOrNotOwned_isNotFound() throws Exception {
        willThrow(new QuizNotFoundException(QuizId.generate())).given(removeCategoryUseCase).removeCategory(any());

        mockMvc.perform(delete(category())
                        .header("Authorization", "Bearer " + tokenFor(ALICE)))
                .andExpect(status().isNotFound());
    }

    @Test
    void removeCategory_withNonUuidIds_isBadRequest() throws Exception {
        mockMvc.perform(delete(ApiPaths.Creator.BASE + "/not-a-uuid/categories/" + categoryId)
                        .header("Authorization", "Bearer " + tokenFor(ALICE)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(removeCategoryUseCase);
    }

    // ---------- helpers ----------

    private String tokenFor(String subject) throws Exception {
        String body = mockMvc.perform(post("/api/public/mock-auth/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sub\":\"" + subject + "\",\"roles\":[\"USER\"]}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.access_token");
    }
}