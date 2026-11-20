package com.iagora.backend.modules.financas.contaFinanceira;

import java.math.BigDecimal;

public record ContaFinanceiraRequest(
    String nome,
    String tipo,
    BigDecimal saldoInicial,
    Integer userId
) {}
