package com.iagora.backend.modules.financas.recorrenciaFinanceira;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecorrenciaFinanceiraRepository extends JpaRepository<RecorrenciaFinanceira, Integer> {
    List<RecorrenciaFinanceira> findByUser_IdOrderByIdAsc(Integer userId);

    List<RecorrenciaFinanceira> findByAtivaTrueOrderByIdAsc();
}
