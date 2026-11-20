package com.iagora.backend.modules.financas.metaFinanceira;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MetaFinanceiraResponse(
    Integer id,
    Integer userId,
    String nome,
    BigDecimal valorAtual,
    BigDecimal valorAlvo,
    LocalDate prazo,
    String status
) {}
