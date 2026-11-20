package com.iagora.backend.modules.casa.tarefa.execucaoTarefa;

import java.time.LocalDateTime;

public record ExecucaoTarefaRequest(
    LocalDateTime dataPrevista
) {}
