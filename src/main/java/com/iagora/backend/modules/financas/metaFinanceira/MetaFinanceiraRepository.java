package com.iagora.backend.modules.financas.metaFinanceira;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MetaFinanceiraRepository extends JpaRepository<MetaFinanceira, Integer> {
    List<MetaFinanceira> findByUser_IdOrderByIdAsc(Integer userId);
}
