package com.iagora.backend.modules.preparacao.jornada.jornada;

public record JornadaRequest(
    String nome,
    String descricao,
    Integer userId
) {}
