package com.iagora.backend.modules.preparacao.aprendizado.respostaQuiz;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RespostaQuizRepository extends JpaRepository<RespostaQuiz, Integer> {
    List<RespostaQuiz> findByTentativa_IdOrderByIdAsc(Integer tentativaId);
}
