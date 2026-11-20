package com.iagora.backend.modules.preparacao.moradia.custoMoradia;

import java.math.BigDecimal;

public record CustoMoradiaRequest(
    String nome,
    String categoria,
    String tipo,
    BigDecimal valor
) {}
