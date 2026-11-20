package com.iagora.backend.modules.casa.tarefa.execucaoTarefa;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExecucaoTarefaRepository extends JpaRepository<ExecucaoTarefa, Integer> {
    List<ExecucaoTarefa> findByTarefa_Categoria_User_IdOrderByIdAsc(Integer userId);
    List<ExecucaoTarefa> findByTarefa_IdOrderByIdAsc(Integer tarefaId);

    void deleteByTarefa_IdAndGeradaTrueAndStatusAndDataPrevistaGreaterThanEqual(Integer tarefaId, String status, LocalDateTime dataPrevista);

    boolean existsByTarefa_IdAndDataPrevista(Integer tarefaId, LocalDateTime dataPrevista);
}
