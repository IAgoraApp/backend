package com.iagora.backend.modules.casa.limpeza.comodo;

public record ComodoResponse(
    Integer id,
    Integer casaId,
    String nome,
    String tipo,
    String descricao
) {}
