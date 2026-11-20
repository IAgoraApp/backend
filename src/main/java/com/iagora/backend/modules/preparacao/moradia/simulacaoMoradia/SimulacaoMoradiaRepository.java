package com.iagora.backend.modules.preparacao.moradia.simulacaoMoradia;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SimulacaoMoradiaRepository extends JpaRepository<SimulacaoMoradia, Integer> {
    List<SimulacaoMoradia> findByPlano_IdOrderByIdAsc(Integer planoId);
}
