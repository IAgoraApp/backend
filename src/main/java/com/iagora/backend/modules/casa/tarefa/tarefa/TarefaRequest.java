package com.iagora.backend.modules.casa.tarefa.tarefa;

import java.time.LocalDateTime;

public record TarefaRequest(
    Integer categoriaId,
    String nome,
    String descricao,
    LocalDateTime prazo,
    Integer lembreteMinutos
) {
    public TarefaRequest(Integer categoriaId, String nome, String descricao, LocalDateTime prazo) {
        this(categoriaId, nome, descricao, prazo, null);
    }
}
