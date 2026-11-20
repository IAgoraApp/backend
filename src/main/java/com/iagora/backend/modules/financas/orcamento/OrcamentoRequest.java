package com.iagora.backend.modules.financas.orcamento;

import java.math.BigDecimal;
import java.time.LocalDate;

public record OrcamentoRequest(
    Integer categoriaId,
    LocalDate mes,
    BigDecimal limite,
    Integer userId
) {}
