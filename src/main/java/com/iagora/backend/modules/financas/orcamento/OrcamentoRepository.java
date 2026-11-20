package com.iagora.backend.modules.financas.orcamento;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrcamentoRepository extends JpaRepository<Orcamento, Integer> {
    boolean existsByCategoria_Id(Integer categoriaId);
    List<Orcamento> findByUser_IdOrderByIdAsc(Integer userId);
}
