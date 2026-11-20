package com.iagora.backend.modules.casa.tarefa.tarefa;

import java.time.LocalDateTime;

public record TarefaResponse(
    Integer id,
    Integer categoriaId,
    String nome,
    String descricao,
    LocalDateTime prazo,
    Integer lembreteMinutos
) {}
