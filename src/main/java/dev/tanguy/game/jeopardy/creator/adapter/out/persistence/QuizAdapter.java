package dev.tanguy.game.jeopardy.creator.adapter.out.persistence;

import dev.tanguy.game.jeopardy.common.domain.model.id.OwnerId;
import dev.tanguy.game.jeopardy.common.domain.model.id.QuizId;
import dev.tanguy.game.jeopardy.creator.domain.model.Quiz;
import dev.tanguy.game.jeopardy.creator.port.out.QuizRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
        QuizJpaEntity saved = quizJpaRepository.save(quizMapper.toJpaEntity(quiz));
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
}
