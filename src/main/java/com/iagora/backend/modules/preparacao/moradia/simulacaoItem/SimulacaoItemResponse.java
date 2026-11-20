package com.iagora.backend.modules.preparacao.moradia.simulacaoItem;

import java.math.BigDecimal;

public record SimulacaoItemResponse(
    Integer id,
    Integer simulacaoId,
    String nome,
    String categoria,
    String tipo,
    BigDecimal valor
) {}
