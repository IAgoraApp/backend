package com.iagora.backend.modules.preparacao.gastoPreparacao;

import java.math.BigDecimal;

public record GastoPreparacaoRequest(
    String nome,
    BigDecimal valor,
    String frequencia,
    String destino,
    Integer userId
) {}
