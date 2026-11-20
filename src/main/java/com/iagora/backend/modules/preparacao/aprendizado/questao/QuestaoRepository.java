package com.iagora.backend.modules.preparacao.aprendizado.questao;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestaoRepository extends JpaRepository<Questao, Integer> {
    List<Questao> findByQuiz_IdOrderByIdAsc(Integer quizId);
}
