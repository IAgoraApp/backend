package com.iagora.backend.modules.financas.transacaoFinanceira;

import java.math.BigDecimal;

public record ResumoFinanceiroResponse(
    BigDecimal receitas,
    BigDecimal despesas,
    BigDecimal saldo,
    BigDecimal receitasPendentes,
    BigDecimal despesasPendentes
) {}
