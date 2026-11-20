package com.iagora.backend.modules.preparacao.jornada.tarefaJornada;

public record TarefaJornadaRequest(
    String titulo,
    String descricao,
    Integer ordem
) {}
