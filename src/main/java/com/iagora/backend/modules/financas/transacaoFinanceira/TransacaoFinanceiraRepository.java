package com.iagora.backend.modules.financas.transacaoFinanceira;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransacaoFinanceiraRepository extends JpaRepository<TransacaoFinanceira, Integer> {
    boolean existsByCategoria_Id(Integer categoriaId);
    List<TransacaoFinanceira> findByUser_IdOrderByIdAsc(Integer userId);

    List<TransacaoFinanceira> findByConta_IdOrderByIdAsc(Integer contaId);

    boolean existsByRecorrencia_IdAndCompetencia(Integer recorrenciaId, LocalDate competencia);
    List<TransacaoFinanceira> findByRecorrencia_Id(Integer recorrenciaId);
}
