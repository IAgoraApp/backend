package com.iagora.backend.modules.preparacao.jornada.etapaJornada;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EtapaJornadaRepository extends JpaRepository<EtapaJornada, Integer> {
    List<EtapaJornada> findByJornada_IdOrderByOrdemAscIdAsc(Integer jornadaId);
}
