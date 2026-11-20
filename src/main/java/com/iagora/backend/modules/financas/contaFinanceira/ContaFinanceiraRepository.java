package com.iagora.backend.modules.financas.contaFinanceira;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContaFinanceiraRepository extends JpaRepository<ContaFinanceira, Integer> {
    List<ContaFinanceira> findByUser_IdOrderByIdAsc(Integer userId);
}
