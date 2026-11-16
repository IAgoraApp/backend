package com.iagora.backend.modules.identidade.perfil;

public record PerfilResponse(
    Integer id,
    Integer userId,
    Integer enderecoId,
    String nome,
    String foto,
    Integer tipo
) {}
