package com.iagora.backend.modules.comercial.pagamento;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PagamentoResponse(
    Integer id,
    Integer userId,
    Integer metodoId,
    Integer assinaturaId,
    Integer publicacaoId,
    BigDecimal valor,
    String status,
    String gateway,
    String transacaoExternaId,
    LocalDateTime criadoEm,
    LocalDateTime pagoEm
) {}
