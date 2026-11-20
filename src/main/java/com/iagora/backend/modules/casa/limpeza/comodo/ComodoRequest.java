package com.iagora.backend.modules.casa.limpeza.comodo;

public record ComodoRequest(
    Integer casaId,
    String nome,
    String tipo,
    String descricao
) {}
