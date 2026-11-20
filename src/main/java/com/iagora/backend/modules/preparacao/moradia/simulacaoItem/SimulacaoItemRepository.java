package com.iagora.backend.modules.preparacao.moradia.simulacaoItem;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SimulacaoItemRepository extends JpaRepository<SimulacaoItem, Integer> {
    List<SimulacaoItem> findBySimulacao_IdOrderByIdAsc(Integer simulacaoId);
}
