package dev.tanguy.game.jeopardy.creator.adapter.in.web;

import dev.tanguy.game.jeopardy.common.domain.model.id.QuizId;
import dev.tanguy.game.jeopardy.common.infrastructure.security.CurrentUser;
import dev.tanguy.game.jeopardy.common.infrastructure.security.SecurityConfig;
import dev.tanguy.game.jeopardy.common.infrastructure.security.mock.MockAuthController;
import dev.tanguy.game.jeopardy.common.infrastructure.security.mock.MockJwtConfig;
import dev.tanguy.game.jeopardy.common.infrastructure.security.mock.MockTokenService;
import dev.tanguy.game.jeopardy.common.web.ApiPaths;
import dev.tanguy.game.jeopardy.creator.domain.model.Quiz;
import dev.tanguy.game.jeopardy.creator.port.in.quiz.CreateQuizCommand;
import dev.tanguy.game.jeopardy.creator.port.in.quiz.CreateQuizUseCase;
import com.jayway.jsonpath.JsonPath;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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

    @BeforeEach
    void stubUseCase() {
        given(createQuizUseCase.createQuiz(any())).willAnswer(invocation -> {
            CreateQuizCommand command = invocation.getArgument(0);
            return new Quiz(QuizId.generate(), command.ownerId(), command.name());
        });
    }

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
                .andExpect(jsonPath("$.data.id").exists());

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

    private String tokenFor(String subject) throws Exception {
        String body = mockMvc.perform(post("/api/public/mock-auth/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sub\":\"" + subject + "\",\"roles\":[\"USER\"]}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.access_token");
    }
}