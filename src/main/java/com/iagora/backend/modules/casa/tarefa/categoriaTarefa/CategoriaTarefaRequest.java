package com.iagora.backend.modules.casa.tarefa.categoriaTarefa;

public record CategoriaTarefaRequest(
    String nome,
    String cor,
    String icone,
    Integer userId
) {}
