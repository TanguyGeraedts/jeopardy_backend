package dev.tanguy.game.jeopardy.creator.adapter.out.persistence;

import dev.tanguy.game.jeopardy.common.domain.model.id.CategoryId;
import dev.tanguy.game.jeopardy.common.domain.model.id.OwnerId;
import dev.tanguy.game.jeopardy.common.domain.model.id.QuestionId;
import dev.tanguy.game.jeopardy.common.domain.model.id.QuizId;
import dev.tanguy.game.jeopardy.creator.domain.model.Category;
import dev.tanguy.game.jeopardy.creator.domain.model.Question;
import dev.tanguy.game.jeopardy.creator.domain.model.Quiz;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Maps between the Quiz aggregate and its JPA entities.
 * Domain ids are String-based value objects (OwnerId excepted), the tables use UUID columns.
 */
@Component
public class QuizMapper {

    // ---------- JPA -> domain ----------

    public Quiz toDomain(QuizJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        QuizId quizId = new QuizId(entity.getId().toString());

        List<Category> categories = entity.getCategories().stream()
                .map(categoryEntity -> toCategoryDomain(categoryEntity, quizId))
                .toList();

        return new Quiz(
                quizId,
                new OwnerId(entity.getOwnerId()),
                entity.getName(),
                categories
        );
    }

    private Category toCategoryDomain(CategoryJpaEntity entity, QuizId quizId) {
        CategoryId categoryId = new CategoryId(entity.getId().toString());

        List<Question> questions = entity.getQuestions().stream()
                .map(questionEntity -> toQuestionDomain(questionEntity, categoryId))
                .toList();

        return new Category(categoryId, quizId, entity.getName(), questions);
    }

    private Question toQuestionDomain(QuestionJpaEntity entity, CategoryId categoryId) {
        return new Question(
                new QuestionId(entity.getId().toString()),
                categoryId,
                entity.getPoints(),
                entity.getQuestionText(),
                entity.getAnswerText(),
                entity.getAnswerType(),
                entity.getMediaUrl(),
                entity.isDailyDouble()
        );
    }

    // ---------- domain -> JPA ----------

    public QuizJpaEntity toJpaEntity(Quiz domain) {
        if (domain == null) {
            return null;
        }

        QuizJpaEntity quizEntity = QuizJpaEntity.builder()
                .id(UUID.fromString(domain.getId().value()))
                .ownerId(domain.getOwnerId().value())
                .name(domain.getName())
                .build();

        List<Category> categories = domain.getCategories();
        for (int i = 0; i < categories.size(); i++) {
            quizEntity.addCategory(toCategoryJpaEntity(categories.get(i), quizEntity, i));
        }

        return quizEntity;
    }

    private CategoryJpaEntity toCategoryJpaEntity(Category domain, QuizJpaEntity quizEntity, int sortOrder) {
        CategoryJpaEntity categoryEntity = CategoryJpaEntity.builder()
                .id(UUID.fromString(domain.getId().value()))
                .quiz(quizEntity)
                .name(domain.getName())
                .sortOrder(sortOrder)
                .build();

        domain.getQuestions().forEach(question ->
                categoryEntity.addQuestion(toQuestionJpaEntity(question, categoryEntity)));

        return categoryEntity;
    }

    private QuestionJpaEntity toQuestionJpaEntity(Question domain, CategoryJpaEntity categoryEntity) {
        return QuestionJpaEntity.builder()
                .id(UUID.fromString(domain.getId().value()))
                .category(categoryEntity)
                .points(domain.getPoints())
                .questionText(domain.getQuestionText())
                .answerText(domain.getAnswerText())
                .answerType(domain.getAnswerType())
                .mediaUrl(domain.getMediaUrl())
                .isDailyDouble(domain.isDailyDouble())
                .build();
    }
}
