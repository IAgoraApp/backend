package com.iagora.backend.modules.preparacao.aprendizado.quiz;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizRepository extends JpaRepository<Quiz, Integer> {
    List<Quiz> findByAula_IdOrderByIdAsc(Integer aulaId);
}
