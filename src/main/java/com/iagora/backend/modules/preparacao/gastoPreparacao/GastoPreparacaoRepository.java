package com.iagora.backend.modules.preparacao.gastoPreparacao;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GastoPreparacaoRepository extends JpaRepository<GastoPreparacao, Integer> {
    List<GastoPreparacao> findByUser_IdOrderByIdAsc(Integer userId);
}
