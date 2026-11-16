package com.iagora.backend.modules.casa.cozinha.receita;

public record ReceitaRequest(
    Integer userId,
    Boolean publica,
    String nome,
    String descricao,
    String tipo,
    String preparo
) {}
