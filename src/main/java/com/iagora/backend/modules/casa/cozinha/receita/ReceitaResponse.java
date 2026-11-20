package com.iagora.backend.modules.casa.cozinha.receita;

public record ReceitaResponse(
    Integer id,
    Integer userId,
    Boolean publica,
    String nome,
    String descricao,
    String tipo,
    String preparo,
    String imagem,
    String statusPublicacao
) {}
