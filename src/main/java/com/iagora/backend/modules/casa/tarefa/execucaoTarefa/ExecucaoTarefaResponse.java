package com.iagora.backend.modules.casa.tarefa.execucaoTarefa;

import java.time.LocalDateTime;

public record ExecucaoTarefaResponse(
    Integer id,
    Integer tarefaId,
    LocalDateTime dataPrevista,
    LocalDateTime concluidaEm,
    String status
) {}
