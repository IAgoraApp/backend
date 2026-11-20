package com.iagora.backend.modules.preparacao.moradia.simulacaoItem;

import java.math.BigDecimal;

public record SimulacaoItemRequest(
    String nome,
    String categoria,
    String tipo,
    BigDecimal valor
) {}
