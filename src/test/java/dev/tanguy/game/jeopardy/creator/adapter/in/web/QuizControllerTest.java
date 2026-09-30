package dev.tanguy.game.jeopardy.creator.adapter.in.web;

import com.jayway.jsonpath.JsonPath;
import dev.tanguy.game.jeopardy.common.domain.model.id.CategoryId;
import dev.tanguy.game.jeopardy.common.domain.model.id.OwnerId;
import dev.tanguy.game.jeopardy.common.domain.model.id.QuestionId;
import dev.tanguy.game.jeopardy.common.domain.model.id.QuizId;
import dev.tanguy.game.jeopardy.common.infrastructure.security.CurrentUser;
import dev.tanguy.game.jeopardy.common.infrastructure.security.SecurityConfig;
import dev.tanguy.game.jeopardy.common.infrastructure.security.mock.MockAuthController;
import dev.tanguy.game.jeopardy.common.infrastructure.security.mock.MockJwtConfig;
import dev.tanguy.game.jeopardy.common.infrastructure.security.mock.MockTokenService;
import dev.tanguy.game.jeopardy.common.web.ApiPaths;
import dev.tanguy.game.jeopardy.creator.domain.event.quiz.QuizNotFoundException;
import dev.tanguy.game.jeopardy.creator.domain.model.AnswerType;
import dev.tanguy.game.jeopardy.creator.domain.model.Category;
import dev.tanguy.game.jeopardy.creator.domain.model.Question;
import dev.tanguy.game.jeopardy.creator.domain.model.Quiz;
import dev.tanguy.game.jeopardy.creator.port.in.quiz.CreateQuizCommand;
import dev.tanguy.game.jeopardy.creator.port.in.quiz.CreateQuizUseCase;
import dev.tanguy.game.jeopardy.creator.port.in.quiz.GetQuizQuery;
import dev.tanguy.game.jeopardy.creator.port.in.quiz.GetQuizUseCase;
import dev.tanguy.game.jeopardy.creator.port.in.quiz.GetQuizzesByOwnerQuery;
import dev.tanguy.game.jeopardy.creator.port.in.quiz.GetQuizzesByOwnerUseCase;
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

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {QuizController.class, MockAuthController.class})
@Import({SecurityConfig.class, MockJwtConfig.class, MockTokenService.class, CurrentUser.class})
@ActiveProfiles("dev")
class QuizControllerTest {

    private static final String QUIZZES = ApiPaths.Creator.BASE;
    private static final String ALICE = "11111111-1111-1111-1111-111111111111";

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    CreateQuizUseCase createQuizUseCase;

    @MockitoBean
    GetQuizUseCase getQuizUseCase;

    @MockitoBean
    GetQuizzesByOwnerUseCase getQuizzesByOwnerUseCase;

    @BeforeEach
    void stubCreate() {
        given(createQuizUseCase.createQuiz(any())).willAnswer(invocation -> {
            CreateQuizCommand command = invocation.getArgument(0);
            return new Quiz(QuizId.generate(), command.ownerId(), command.name());
        });
    }

    // ---------- create ----------

    @Test
    void createQuiz_withoutToken_isUnauthorized() throws Exception {
        mockMvc.perform(post(QUIZZES).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Trivia\"}"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(createQuizUseCase);
    }

    @Test
    void createQuiz_takesOwnerFromToken_andIgnoresOwnerInBody() throws Exception {
        String token = tokenFor(ALICE);

        mockMvc.perform(post(QUIZZES)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"  General Knowledge  \",\"ownerId\":\"99999999-9999-9999-9999-999999999999\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("General Knowledge"))
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.categories").isEmpty());

        ArgumentCaptor<CreateQuizCommand> captor = ArgumentCaptor.forClass(CreateQuizCommand.class);
        verify(createQuizUseCase).createQuiz(captor.capture());
        assertThat(captor.getValue().ownerId().value()).isEqualTo(UUID.fromString(ALICE));
        assertThat(captor.getValue().name()).isEqualTo("General Knowledge");
    }

    @Test
    void createQuiz_withBlankName_isBadRequest() throws Exception {
        mockMvc.perform(post(QUIZZES)
                        .header("Authorization", "Bearer " + tokenFor(ALICE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Failed"))
                .andExpect(jsonPath("$.errors.name").exists());

        verifyNoInteractions(createQuizUseCase);
    }

    @Test
    void createQuiz_withTooLongName_isBadRequest() throws Exception {
        mockMvc.perform(post(QUIZZES)
                        .header("Authorization", "Bearer " + tokenFor(ALICE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + "a".repeat(256) + "\"}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(createQuizUseCase);
    }

    @Test
    void createQuiz_withMalformedJson_isBadRequest() throws Exception {
        mockMvc.perform(post(QUIZZES)
                        .header("Authorization", "Bearer " + tokenFor(ALICE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{bad"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(createQuizUseCase);
    }

    // ---------- get ----------

    @Test
    void getQuiz_withoutToken_isUnauthorized() throws Exception {
        mockMvc.perform(get(QUIZZES + "/" + UUID.randomUUID()))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(getQuizUseCase);
    }

    @Test
    void getQuiz_returnsFullQuiz_andAsksOnBehalfOfTheCaller() throws Exception {
        Quiz quiz = sampleQuiz();
        given(getQuizUseCase.getQuiz(any())).willReturn(quiz);

        mockMvc.perform(get(QUIZZES + "/" + quiz.getId().value())
                        .header("Authorization", "Bearer " + tokenFor(ALICE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(quiz.getId().value()))
                .andExpect(jsonPath("$.data.name").value("Trivia"))
                .andExpect(jsonPath("$.data.categories[0].name").value("Science"))
                .andExpect(jsonPath("$.data.categories[0].questions[0].points").value(100))
                .andExpect(jsonPath("$.data.categories[0].questions[0].questionText").value("What is H2O?"))
                .andExpect(jsonPath("$.data.categories[0].questions[0].answerText").value("Water"))
                .andExpect(jsonPath("$.data.categories[0].questions[0].answerType").value("TEXT"))
                .andExpect(jsonPath("$.data.categories[0].questions[0].dailyDouble").value(false));

        ArgumentCaptor<GetQuizQuery> captor = ArgumentCaptor.forClass(GetQuizQuery.class);
        verify(getQuizUseCase).getQuiz(captor.capture());
        assertThat(captor.getValue().quizId()).isEqualTo(quiz.getId());
        assertThat(captor.getValue().requesterId().value()).isEqualTo(UUID.fromString(ALICE));
    }

    @Test
    void getQuiz_whenUseCaseSaysNotFound_isNotFound() throws Exception {
        given(getQuizUseCase.getQuiz(any())).willThrow(new QuizNotFoundException(QuizId.generate()));

        mockMvc.perform(get(QUIZZES + "/" + UUID.randomUUID())
                        .header("Authorization", "Bearer " + tokenFor(ALICE)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource Not Found"));
    }

    @Test
    void getQuiz_withNonUuidId_isBadRequest() throws Exception {
        mockMvc.perform(get(QUIZZES + "/not-a-uuid")
                        .header("Authorization", "Bearer " + tokenFor(ALICE)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(getQuizUseCase);
    }

    // ---------- get my quizzes ----------

    @Test
    void getAllMyQuizzes_withoutToken_isUnauthorized() throws Exception {
        mockMvc.perform(get(QUIZZES + ApiPaths.Creator.ME))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(getQuizzesByOwnerUseCase);
    }

    @Test
    void getAllMyQuizzes_returnsQuizzesForAuthenticatedUser() throws Exception {
        Quiz quiz1 = sampleQuiz();
        Quiz quiz2 = new Quiz(QuizId.generate(), OwnerId.of(ALICE), "Pop Culture");
        given(getQuizzesByOwnerUseCase.getMyQuizzes(any())).willReturn(List.of(quiz1, quiz2));

        mockMvc.perform(get(QUIZZES + ApiPaths.Creator.ME)
                        .header("Authorization", "Bearer " + tokenFor(ALICE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].id").value(quiz1.getId().value()))
                .andExpect(jsonPath("$.data[0].name").value("Trivia"))
                .andExpect(jsonPath("$.data[1].id").value(quiz2.getId().value()))
                .andExpect(jsonPath("$.data[1].name").value("Pop Culture"));

        ArgumentCaptor<GetQuizzesByOwnerQuery> captor = ArgumentCaptor.forClass(GetQuizzesByOwnerQuery.class);
        verify(getQuizzesByOwnerUseCase).getMyQuizzes(captor.capture());
        assertThat(captor.getValue().ownerId().value()).isEqualTo(UUID.fromString(ALICE));
    }

    @Test
    void getAllMyQuizzes_whenUserHasNoQuizzes_returnsEmptyList() throws Exception {
        given(getQuizzesByOwnerUseCase.getMyQuizzes(any())).willReturn(List.of());

        mockMvc.perform(get(QUIZZES + ApiPaths.Creator.ME)
                        .header("Authorization", "Bearer " + tokenFor(ALICE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty());

        verify(getQuizzesByOwnerUseCase).getMyQuizzes(any());
    }

    // ---------- helpers ----------

    private static Quiz sampleQuiz() {
        QuizId quizId = QuizId.generate();
        Quiz quiz = new Quiz(quizId, OwnerId.of(ALICE), "Trivia");

        CategoryId categoryId = CategoryId.generate();
        Category category = new Category(categoryId, quizId, "Science");
        category.addQuestion(new Question(
                QuestionId.generate(), categoryId, 100, "What is H2O?", "Water", AnswerType.TEXT, null));
        quiz.addCategory(category);
        return quiz;
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