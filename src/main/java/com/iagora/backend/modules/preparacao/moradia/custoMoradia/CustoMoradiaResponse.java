package com.iagora.backend.modules.preparacao.moradia.custoMoradia;

import java.math.BigDecimal;

public record CustoMoradiaResponse(
    Integer id,
    Integer planoId,
    String nome,
    String categoria,
    String tipo,
    BigDecimal valor
) {}
