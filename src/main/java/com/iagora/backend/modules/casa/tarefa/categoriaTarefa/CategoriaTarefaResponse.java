package com.iagora.backend.modules.casa.tarefa.categoriaTarefa;

public record CategoriaTarefaResponse(
    Integer id,
    Integer userId,
    String nome,
    String cor,
    String icone
) {}
