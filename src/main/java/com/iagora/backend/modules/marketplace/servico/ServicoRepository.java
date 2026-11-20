package com.iagora.backend.modules.marketplace.servico;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServicoRepository extends JpaRepository<Servico, Integer> {
    List<Servico> findByEmpresa_User_IdOrderByIdAsc(Integer userId);
    List<Servico> findByStatusPublicacaoOrderByIdAsc(String status);
    List<Servico> findByAtivoTrueAndValidadoTrueAndExpiraEmAfterOrderByIdAsc(LocalDateTime momento);
}
