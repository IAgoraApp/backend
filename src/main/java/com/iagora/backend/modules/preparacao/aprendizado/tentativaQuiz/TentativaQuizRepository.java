package com.iagora.backend.modules.preparacao.aprendizado.tentativaQuiz;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TentativaQuizRepository extends JpaRepository<TentativaQuiz, Integer> {
    List<TentativaQuiz> findByQuiz_IdAndUser_IdOrderByIdDesc(Integer quizId, Integer userId);
}
