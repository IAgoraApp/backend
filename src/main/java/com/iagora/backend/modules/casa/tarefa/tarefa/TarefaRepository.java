package com.iagora.backend.modules.casa.tarefa.tarefa;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TarefaRepository extends JpaRepository<Tarefa, Integer> {
    List<Tarefa> findByCategoria_User_IdOrderByIdAsc(Integer userId);
    List<Tarefa> findByCategoria_IdOrderByIdAsc(Integer categoriaId);
    boolean existsByCategoria_Id(Integer categoriaId);
}
