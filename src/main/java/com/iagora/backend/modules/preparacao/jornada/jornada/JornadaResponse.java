package com.iagora.backend.modules.preparacao.jornada.jornada;

import java.time.LocalDateTime;

public record JornadaResponse(
    Integer id,
    Integer userId,
    String nome,
    String descricao,
    String status,
    LocalDateTime criadaEm
) {}
