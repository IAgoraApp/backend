package com.iagora.backend.modules.financas.recorrenciaFinanceira;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RecorrenciaFinanceiraRequest(
    String descricao,
    String tipo,
    BigDecimal valorPrevisto,
    String frequencia,
    LocalDate inicioEm,
    LocalDate fimEm,
    Integer userId
) {}
