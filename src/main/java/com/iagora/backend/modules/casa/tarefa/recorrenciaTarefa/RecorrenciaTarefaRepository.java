package com.iagora.backend.modules.casa.tarefa.recorrenciaTarefa;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecorrenciaTarefaRepository extends JpaRepository<RecorrenciaTarefa, Integer> {
    Optional<RecorrenciaTarefa> findByTarefa_Id(Integer tarefaId);
}
