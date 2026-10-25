package dev.tanguy.game.jeopardy.creator.adapter.out.persistence;

import dev.tanguy.game.jeopardy.common.domain.model.id.OwnerId;
import dev.tanguy.game.jeopardy.common.domain.model.id.QuizId;
import dev.tanguy.game.jeopardy.creator.domain.model.Quiz;
import dev.tanguy.game.jeopardy.creator.domain.model.DiscoverSort;
import dev.tanguy.game.jeopardy.creator.domain.model.QuizTopic;
import dev.tanguy.game.jeopardy.creator.port.out.QuizRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class QuizAdapter implements QuizRepositoryPort {

    private final QuizJpaRepository quizJpaRepository;
    private final QuizMapper quizMapper;

    @Override
    @Transactional
    public Quiz save(Quiz quiz) {
        QuizJpaEntity entity = quizMapper.toJpaEntity(quiz);
        // Every save is an edit (rename, questions, topics, publishing): "recently edited" on My quizzes.
        Instant now = Instant.now();
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(now);
        }
        entity.setUpdatedAt(now);
        QuizJpaEntity saved = quizJpaRepository.save(entity);
        // Map inside the transaction: the saved graph is still attached.
        return quizMapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Quiz> findById(QuizId quizId) {
        return quizJpaRepository.findById(UUID.fromString(quizId.value()))
                .map(quizMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Quiz> findByOwner(OwnerId ownerId) {
        return quizJpaRepository.findByOwnerId(ownerId.value()).stream()
                .map(quizMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public void deleteById(QuizId quizId) {
        quizJpaRepository.deleteById(UUID.fromString(quizId.value()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Quiz> searchPublished(String text, QuizTopic topic, DiscoverSort sort, int limit) {
        String pattern = text == null || text.isBlank()
                ? "%"
                : "%" + text.strip().toLowerCase(Locale.ROOT).replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
        Sort order = sort == DiscoverSort.NEWEST
                ? Sort.by(Sort.Order.desc("publishedAt"))
                : Sort.by(Sort.Order.desc("timesPlayed"), Sort.Order.desc("publishedAt"));
        // Any topic: the topic value is ignored, but Postgres still wants a typed value.
        return quizJpaRepository.searchPublished(pattern, topic == null, topic == null ? QuizTopic.GENERAL_KNOWLEDGE : topic,
                        PageRequest.of(0, limit, order)).stream()
                .map(quizMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public void incrementTimesPlayed(QuizId quizId) {
        quizJpaRepository.incrementTimesPlayed(UUID.fromString(quizId.value()));
    }
}
