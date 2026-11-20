package com.iagora.backend.modules.comercial.planoPublicacao;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanoPublicacaoRepository extends JpaRepository<PlanoPublicacao, Integer> {
    List<PlanoPublicacao> findByAtivoTrueOrderByIdAsc();
}
