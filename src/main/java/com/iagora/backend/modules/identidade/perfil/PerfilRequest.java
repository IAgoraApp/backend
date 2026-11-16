package com.iagora.backend.modules.identidade.perfil;

public record PerfilRequest(
    String nome,
    String foto,
    Integer tipo,
    Integer enderecoId
) {}
