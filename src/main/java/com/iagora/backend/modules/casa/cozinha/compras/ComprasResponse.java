package com.iagora.backend.modules.casa.cozinha.compras;

import java.time.LocalDate;

public record ComprasResponse(
    Integer id,
    Integer casaId,
    String nome,
    LocalDate criacao
) {}
