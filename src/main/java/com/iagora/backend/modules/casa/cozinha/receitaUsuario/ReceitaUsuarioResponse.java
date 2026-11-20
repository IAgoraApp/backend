package com.iagora.backend.modules.casa.cozinha.receitaUsuario;

public record ReceitaUsuarioResponse(
    Integer id,
    Integer userId,
    Integer receitaId,
    String status
) {}
