package com.iagora.backend.modules.preparacao.jornada.tarefaJornada;

public record TarefaJornadaResponse(
    Integer id,
    Integer etapaId,
    String titulo,
    String descricao,
    Boolean concluida,
    Integer ordem
) {}
