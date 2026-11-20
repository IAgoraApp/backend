package com.iagora.backend.modules.casa.tarefa.tarefa;

import java.time.LocalDateTime;

public record TarefaRequest(
    Integer categoriaId,
    String nome,
    String descricao,
    LocalDateTime prazo
) {}
