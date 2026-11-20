package com.iagora.backend.modules.financas.orcamento;

import java.math.BigDecimal;
import java.time.LocalDate;

public record OrcamentoResponse(
    Integer id,
    Integer userId,
    Integer categoriaId,
    LocalDate mes,
    BigDecimal limite
) {}
