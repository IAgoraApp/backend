package com.iagora.backend.modules.preparacao.jornada.tarefaJornada;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TarefaJornadaRepository extends JpaRepository<TarefaJornada, Integer> {
    List<TarefaJornada> findByEtapa_IdOrderByOrdemAscIdAsc(Integer etapaId);
}
