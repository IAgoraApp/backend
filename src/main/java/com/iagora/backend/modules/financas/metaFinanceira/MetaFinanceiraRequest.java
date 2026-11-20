package com.iagora.backend.modules.financas.metaFinanceira;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MetaFinanceiraRequest(
    String nome,
    BigDecimal valorAtual,
    BigDecimal valorAlvo,
    LocalDate prazo,
    Integer userId
) {}
