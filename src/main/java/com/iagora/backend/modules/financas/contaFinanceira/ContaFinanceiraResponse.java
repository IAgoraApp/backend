package com.iagora.backend.modules.financas.contaFinanceira;

import java.math.BigDecimal;

public record ContaFinanceiraResponse(
    Integer id,
    Integer userId,
    String nome,
    String tipo,
    BigDecimal saldoInicial,
    BigDecimal saldoAtual
) {}
