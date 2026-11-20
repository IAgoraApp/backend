package com.iagora.backend.modules.preparacao.gastoPreparacao;

import java.math.BigDecimal;

public record GastoPreparacaoResponse(
    Integer id,
    Integer userId,
    String nome,
    BigDecimal valor,
    String frequencia,
    String destino
) {}
