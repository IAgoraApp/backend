package com.iagora.backend.modules.casa.tarefa.categoriaTarefa;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaTarefaRepository extends JpaRepository<CategoriaTarefa, Integer> {
    List<CategoriaTarefa> findByUser_IdOrderByIdAsc(Integer userId);
}
