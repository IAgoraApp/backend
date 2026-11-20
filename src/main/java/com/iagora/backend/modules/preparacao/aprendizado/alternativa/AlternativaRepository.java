package com.iagora.backend.modules.preparacao.aprendizado.alternativa;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlternativaRepository extends JpaRepository<Alternativa, Integer> {
    List<Alternativa> findByQuestao_IdOrderByIdAsc(Integer questaoId);
}
