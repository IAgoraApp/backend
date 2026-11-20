package com.iagora.backend.modules.financas.transacaoFinanceira;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransacaoFinanceiraResponse(
    Integer id,
    Integer userId,
    Integer categoriaId,
    Integer contaId,
    Integer recorrenciaId,
    String descricao,
    String tipo,
    BigDecimal valor,
    LocalDate competencia,
    LocalDate vencimento,
    LocalDate pagoEm,
    String status
) {}
