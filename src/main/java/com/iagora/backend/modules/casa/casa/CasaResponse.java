package com.iagora.backend.modules.casa.casa;

public record CasaResponse(
    Integer id,
    Integer userId,
    String nome,
    String tipo,
    Integer enderecoId
) {}
