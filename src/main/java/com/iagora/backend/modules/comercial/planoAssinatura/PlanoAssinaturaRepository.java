package com.iagora.backend.modules.comercial.planoAssinatura;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanoAssinaturaRepository extends JpaRepository<PlanoAssinatura, Integer> {
    List<PlanoAssinatura> findByAtivoTrueOrderByIdAsc();
}
