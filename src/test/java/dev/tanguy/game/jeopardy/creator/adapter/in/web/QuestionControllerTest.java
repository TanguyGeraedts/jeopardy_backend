package dev.tanguy.game.jeopardy.creator.adapter.in.web;

import com.jayway.jsonpath.JsonPath;
import dev.tanguy.game.jeopardy.common.domain.model.id.CategoryId;
import dev.tanguy.game.jeopardy.common.domain.model.id.QuestionId;
import dev.tanguy.game.jeopardy.common.domain.model.id.QuizId;
import dev.tanguy.game.jeopardy.common.infrastructure.security.CurrentUser;
import dev.tanguy.game.jeopardy.common.infrastructure.security.SecurityConfig;
import dev.tanguy.game.jeopardy.common.infrastructure.security.mock.MockAuthController;
import dev.tanguy.game.jeopardy.common.infrastructure.security.mock.MockJwtConfig;
import dev.tanguy.game.jeopardy.common.infrastructure.security.mock.MockTokenService;
import dev.tanguy.game.jeopardy.common.web.ApiPaths;
import dev.tanguy.game.jeopardy.creator.domain.event.category.DuplicateQuestionPointsException;
import dev.tanguy.game.jeopardy.creator.domain.event.category.QuestionNotFoundException;
import dev.tanguy.game.jeopardy.creator.domain.event.question.MissingMediaUrlException;
import dev.tanguy.game.jeopardy.creator.domain.event.quiz.CategoryNotFoundException;
import dev.tanguy.game.jeopardy.creator.domain.event.quiz.QuizNotFoundException;
import dev.tanguy.game.jeopardy.creator.domain.model.AnswerType;
import dev.tanguy.game.jeopardy.creator.domain.model.Question;
import dev.tanguy.game.jeopardy.creator.port.in.question.AddQuestionCommand;
import dev.tanguy.game.jeopardy.creator.port.in.question.AddQuestionUseCase;
import dev.tanguy.game.jeopardy.creator.port.in.question.RemoveQuestionCommand;
import dev.tanguy.game.jeopardy.creator.port.in.question.RemoveQuestionUseCase;
import dev.tanguy.game.jeopardy.creator.port.in.question.UpdateQuestionCommand;
import dev.tanguy.game.jeopardy.creator.port.in.question.UpdateQuestionUseCase;
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

@WebMvcTest(controllers = {QuestionController.class, MockAuthController.class})
@Import({SecurityConfig.class, MockJwtConfig.class, MockTokenService.class, CurrentUser.class})
@ActiveProfiles("dev")
class QuestionControllerTest {

    private static final String ALICE = "11111111-1111-1111-1111-111111111111";

    private static final String VALID_TEXT_BODY = """
            {"points":200,"questionText":"What is H2O?","answerText":"Water","answerType":"TEXT"}""";

    private final UUID quizId = UUID.randomUUID();
    private final UUID categoryId = UUID.randomUUID();
    private final UUID questionId = UUID.randomUUID();

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    AddQuestionUseCase addQuestionUseCase;

    @MockitoBean
    UpdateQuestionUseCase updateQuestionUseCase;

    @MockitoBean
    RemoveQuestionUseCase removeQuestionUseCase;

    @BeforeEach
    void stubUseCases() {
        given(addQuestionUseCase.addQuestion(any())).willAnswer(invocation -> {
            AddQuestionCommand c = invocation.getArgument(0);
            return new Question(QuestionId.generate(), c.categoryId(), c.points(), c.questionText(),
                    c.answerText(), c.answerType(), c.mediaUrl(), c.dailyDouble());
        });

        given(updateQuestionUseCase.updateQuestion(any())).willAnswer(invocation -> {
            UpdateQuestionCommand c = invocation.getArgument(0);
            return new Question(c.questionId(), c.categoryId(), c.points(), c.questionText(),
                    c.answerText(), c.answerType(), c.mediaUrl(), c.dailyDouble());
        });
    }

    private String questions() {
        return ApiPaths.Creator.BASE + "/" + quizId + "/categories/" + categoryId + "/questions";
    }

    private String question() {
        return questions() + "/" + questionId;
    }

    // ---------- add (POST .../questions) ----------

    @Test
    void addQuestion_withoutToken_isUnauthorized() throws Exception {
        mockMvc.perform(post(questions()).contentType(MediaType.APPLICATION_JSON).content(VALID_TEXT_BODY))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(addQuestionUseCase);
    }

    @Test
    void addQuestion_returnsCreated_andMapsEveryFieldToTheCommand() throws Exception {
        String body = """
                {"points":300,"questionText":"  Name this landmark  ","answerText":"  Eiffel Tower  ",
                 "answerType":"IMAGE","mediaUrl":"https://example.com/eiffel.png","dailyDouble":true,
                 "ownerId":"99999999-9999-9999-9999-999999999999"}""";

        mockMvc.perform(post(questions())
                        .header("Authorization", "Bearer " + tokenFor(ALICE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.points").value(300))
                .andExpect(jsonPath("$.data.questionText").value("Name this landmark"))
                .andExpect(jsonPath("$.data.answerText").value("Eiffel Tower"))
                .andExpect(jsonPath("$.data.answerType").value("IMAGE"))
                .andExpect(jsonPath("$.data.mediaUrl").value("https://example.com/eiffel.png"))
                .andExpect(jsonPath("$.data.dailyDouble").value(true));

        ArgumentCaptor<AddQuestionCommand> captor = ArgumentCaptor.forClass(AddQuestionCommand.class);
        verify(addQuestionUseCase).addQuestion(captor.capture());
        AddQuestionCommand command = captor.getValue();
        assertThat(command.quizId().value()).isEqualTo(quizId.toString());
        assertThat(command.categoryId().value()).isEqualTo(categoryId.toString());
        assertThat(command.requesterId().value()).isEqualTo(UUID.fromString(ALICE));
        assertThat(command.points()).isEqualTo(300);
        assertThat(command.answerType()).isEqualTo(AnswerType.IMAGE);
        assertThat(command.dailyDouble()).isTrue();
    }

    @Test
    void addQuestion_dailyDoubleDefaultsToFalse_andBlankMediaUrlBecomesNull() throws Exception {
        String body = """
                {"points":100,"questionText":"Q?","answerText":"A","answerType":"TEXT","mediaUrl":"   "}""";

        mockMvc.perform(post(questions())
                        .header("Authorization", "Bearer " + tokenFor(ALICE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.dailyDouble").value(false));

        ArgumentCaptor<AddQuestionCommand> captor = ArgumentCaptor.forClass(AddQuestionCommand.class);
        verify(addQuestionUseCase).addQuestion(captor.capture());
        assertThat(captor.getValue().mediaUrl()).isNull();
    }

    @Test
    void addQuestion_withZeroPoints_isBadRequest() throws Exception {
        assertBadRequestOnAdd("""
                {"points":0,"questionText":"Q?","answerText":"A","answerType":"TEXT"}""", "points");
    }

    @Test
    void addQuestion_withNegativePoints_isBadRequest() throws Exception {
        assertBadRequestOnAdd("""
                {"points":-100,"questionText":"Q?","answerText":"A","answerType":"TEXT"}""", "points");
    }

    @Test
    void addQuestion_withMissingPoints_isBadRequest() throws Exception {
        // primitive int defaults to 0, which @Positive rejects
        assertBadRequestOnAdd("""
                {"questionText":"Q?","answerText":"A","answerType":"TEXT"}""", "points");
    }

    @Test
    void addQuestion_withBlankQuestionText_isBadRequest() throws Exception {
        assertBadRequestOnAdd("""
                {"points":100,"questionText":"   ","answerText":"A","answerType":"TEXT"}""", "questionText");
    }

    @Test
    void addQuestion_withBlankAnswerText_isBadRequest() throws Exception {
        assertBadRequestOnAdd("""
                {"points":100,"questionText":"Q?","answerText":"   ","answerType":"TEXT"}""", "answerText");
    }

    @Test
    void addQuestion_withTooLongQuestionText_isBadRequest() throws Exception {
        assertBadRequestOnAdd("""
                {"points":100,"questionText":"%s","answerText":"A","answerType":"TEXT"}"""
                .formatted("q".repeat(2001)), "questionText");
    }

    @Test
    void addQuestion_withMissingAnswerType_isBadRequest() throws Exception {
        assertBadRequestOnAdd("""
                {"points":100,"questionText":"Q?","answerText":"A"}""", "answerType");
    }

    @Test
    void addQuestion_withUnknownAnswerType_isBadRequest() throws Exception {
        mockMvc.perform(post(questions())
                        .header("Authorization", "Bearer " + tokenFor(ALICE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"points":100,"questionText":"Q?","answerText":"A","answerType":"AUDIO"}"""))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(addQuestionUseCase);
    }

    @Test
    void addQuestion_withJavascriptMediaUrl_isBadRequest() throws Exception {
        assertBadRequestOnAdd("""
                {"points":100,"questionText":"Q?","answerText":"A","answerType":"IMAGE","mediaUrl":"javascript:alert(1)"}""",
                "mediaUrl");
    }

    @Test
    void addQuestion_withDataUriMediaUrl_isBadRequest() throws Exception {
        assertBadRequestOnAdd("""
                {"points":100,"questionText":"Q?","answerText":"A","answerType":"IMAGE","mediaUrl":"data:text/html;base64,AAAA"}""",
                "mediaUrl");
    }

    @Test
    void addQuestion_withMalformedJson_isBadRequest() throws Exception {
        mockMvc.perform(post(questions())
                        .header("Authorization", "Bearer " + tokenFor(ALICE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{bad"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(addQuestionUseCase);
    }

    @Test
    void addQuestion_withNonUuidCategoryId_isBadRequest() throws Exception {
        mockMvc.perform(post(ApiPaths.Creator.BASE + "/" + quizId + "/categories/not-a-uuid/questions")
                        .header("Authorization", "Bearer " + tokenFor(ALICE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_TEXT_BODY))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(addQuestionUseCase);
    }

    @Test
    void addQuestion_whenQuizIsNotFoundOrNotOwned_isNotFound() throws Exception {
        willThrow(new QuizNotFoundException(QuizId.generate())).given(addQuestionUseCase).addQuestion(any());

        mockMvc.perform(post(questions())
                        .header("Authorization", "Bearer " + tokenFor(ALICE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_TEXT_BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource Not Found"));
    }

    @Test
    void addQuestion_whenCategoryDoesNotExist_isNotFound() throws Exception {
        willThrow(new CategoryNotFoundException(QuizId.generate(), CategoryId.generate()))
                .given(addQuestionUseCase).addQuestion(any());

        mockMvc.perform(post(questions())
                        .header("Authorization", "Bearer " + tokenFor(ALICE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_TEXT_BODY))
                .andExpect(status().isNotFound());
    }

    @Test
    void addQuestion_whenPointsAlreadyUsedInCategory_isConflict() throws Exception {
        willThrow(new DuplicateQuestionPointsException(CategoryId.generate(), 200))
                .given(addQuestionUseCase).addQuestion(any());

        mockMvc.perform(post(questions())
                        .header("Authorization", "Bearer " + tokenFor(ALICE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_TEXT_BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Domain Invariant Conflict"));
    }

    @Test
    void addQuestion_whenImageHasNoMediaUrl_isConflict() throws Exception {
        willThrow(new MissingMediaUrlException(QuestionId.generate(), AnswerType.IMAGE))
                .given(addQuestionUseCase).addQuestion(any());

        mockMvc.perform(post(questions())
                        .header("Authorization", "Bearer " + tokenFor(ALICE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"points":100,"questionText":"Q?","answerText":"A","answerType":"IMAGE"}"""))
                .andExpect(status().isConflict());
    }

    // ---------- update (PUT .../questions/{questionId}) ----------

    @Test
    void updateQuestion_withoutToken_isUnauthorized() throws Exception {
        mockMvc.perform(put(question()).contentType(MediaType.APPLICATION_JSON).content(VALID_TEXT_BODY))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(updateQuestionUseCase);
    }

    @Test
    void updateQuestion_returnsUpdatedQuestion_andPassesAllIdsAndCaller() throws Exception {
        mockMvc.perform(put(question())
                        .header("Authorization", "Bearer " + tokenFor(ALICE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_TEXT_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(questionId.toString()))
                .andExpect(jsonPath("$.data.points").value(200))
                .andExpect(jsonPath("$.data.questionText").value("What is H2O?"))
                .andExpect(jsonPath("$.data.answerText").value("Water"));

        ArgumentCaptor<UpdateQuestionCommand> captor = ArgumentCaptor.forClass(UpdateQuestionCommand.class);
        verify(updateQuestionUseCase).updateQuestion(captor.capture());
        UpdateQuestionCommand command = captor.getValue();
        assertThat(command.quizId().value()).isEqualTo(quizId.toString());
        assertThat(command.categoryId().value()).isEqualTo(categoryId.toString());
        assertThat(command.questionId().value()).isEqualTo(questionId.toString());
        assertThat(command.requesterId().value()).isEqualTo(UUID.fromString(ALICE));
        assertThat(command.points()).isEqualTo(200);
    }

    @Test
    void updateQuestion_withInvalidBody_isBadRequest() throws Exception {
        mockMvc.perform(put(question())
                        .header("Authorization", "Bearer " + tokenFor(ALICE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"points":0,"questionText":"","answerText":"","answerType":"TEXT"}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Failed"))
                .andExpect(jsonPath("$.errors.points").exists())
                .andExpect(jsonPath("$.errors.questionText").exists())
                .andExpect(jsonPath("$.errors.answerText").exists());

        verifyNoInteractions(updateQuestionUseCase);
    }

    @Test
    void updateQuestion_withJavascriptMediaUrl_isBadRequest() throws Exception {
        mockMvc.perform(put(question())
                        .header("Authorization", "Bearer " + tokenFor(ALICE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"points":100,"questionText":"Q?","answerText":"A","answerType":"VIDEO","mediaUrl":"javascript:alert(1)"}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.mediaUrl").exists());

        verifyNoInteractions(updateQuestionUseCase);
    }

    @Test
    void updateQuestion_withNonUuidQuestionId_isBadRequest() throws Exception {
        mockMvc.perform(put(questions() + "/not-a-uuid")
                        .header("Authorization", "Bearer " + tokenFor(ALICE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_TEXT_BODY))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(updateQuestionUseCase);
    }

    @Test
    void updateQuestion_whenQuestionDoesNotExist_isNotFound() throws Exception {
        willThrow(new QuestionNotFoundException(CategoryId.generate(), QuestionId.generate()))
                .given(updateQuestionUseCase).updateQuestion(any());

        mockMvc.perform(put(question())
                        .header("Authorization", "Bearer " + tokenFor(ALICE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_TEXT_BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource Not Found"));
    }

    @Test
    void updateQuestion_whenQuizIsNotFoundOrNotOwned_isNotFound() throws Exception {
        willThrow(new QuizNotFoundException(QuizId.generate())).given(updateQuestionUseCase).updateQuestion(any());

        mockMvc.perform(put(question())
                        .header("Authorization", "Bearer " + tokenFor(ALICE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_TEXT_BODY))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateQuestion_whenPointsTakenByAnotherQuestion_isConflict() throws Exception {
        willThrow(new DuplicateQuestionPointsException(CategoryId.generate(), 200))
                .given(updateQuestionUseCase).updateQuestion(any());

        mockMvc.perform(put(question())
                        .header("Authorization", "Bearer " + tokenFor(ALICE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_TEXT_BODY))
                .andExpect(status().isConflict());
    }

    // ---------- remove (DELETE .../questions/{questionId}) ----------

    @Test
    void removeQuestion_withoutToken_isUnauthorized() throws Exception {
        mockMvc.perform(delete(question()))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(removeQuestionUseCase);
    }

    @Test
    void removeQuestion_returnsNoContent_andPassesAllIdsAndCaller() throws Exception {
        mockMvc.perform(delete(question())
                        .header("Authorization", "Bearer " + tokenFor(ALICE)))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        ArgumentCaptor<RemoveQuestionCommand> captor = ArgumentCaptor.forClass(RemoveQuestionCommand.class);
        verify(removeQuestionUseCase).removeQuestion(captor.capture());
        assertThat(captor.getValue().quizId().value()).isEqualTo(quizId.toString());
        assertThat(captor.getValue().categoryId().value()).isEqualTo(categoryId.toString());
        assertThat(captor.getValue().questionId().value()).isEqualTo(questionId.toString());
        assertThat(captor.getValue().requesterId().value()).isEqualTo(UUID.fromString(ALICE));
    }

    @Test
    void removeQuestion_whenQuestionDoesNotExist_isNotFound() throws Exception {
        willThrow(new QuestionNotFoundException(CategoryId.generate(), QuestionId.generate()))
                .given(removeQuestionUseCase).removeQuestion(any());

        mockMvc.perform(delete(question())
                        .header("Authorization", "Bearer " + tokenFor(ALICE)))
                .andExpect(status().isNotFound());
    }

    @Test
    void removeQuestion_whenQuizIsNotFoundOrNotOwned_isNotFound() throws Exception {
        willThrow(new QuizNotFoundException(QuizId.generate())).given(removeQuestionUseCase).removeQuestion(any());

        mockMvc.perform(delete(question())
                        .header("Authorization", "Bearer " + tokenFor(ALICE)))
                .andExpect(status().isNotFound());
    }

    @Test
    void removeQuestion_withNonUuidIds_isBadRequest() throws Exception {
        mockMvc.perform(delete(questions() + "/not-a-uuid")
                        .header("Authorization", "Bearer " + tokenFor(ALICE)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(removeQuestionUseCase);
    }

    // ---------- helpers ----------

    private void assertBadRequestOnAdd(String body, String invalidField) throws Exception {
        mockMvc.perform(post(questions())
                        .header("Authorization", "Bearer " + tokenFor(ALICE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Failed"))
                .andExpect(jsonPath("$.errors." + invalidField).exists());

        verifyNoInteractions(addQuestionUseCase);
    }

    private String tokenFor(String subject) throws Exception {
        String body = mockMvc.perform(post("/api/public/mock-auth/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sub\":\"" + subject + "\",\"roles\":[\"USER\"]}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.access_token");
    }
}