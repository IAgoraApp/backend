package com.iagora.backend.modules.casa.casa;

public record CasaRequest(
    String nome,
    String tipo,
    Integer enderecoId
) {}
