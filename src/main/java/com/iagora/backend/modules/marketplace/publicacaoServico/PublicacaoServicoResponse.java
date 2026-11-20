package com.iagora.backend.modules.marketplace.publicacaoServico;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PublicacaoServicoResponse(
    Integer id,
    Integer servicoId,
    Integer planoId,
    LocalDateTime inicio,
    LocalDateTime fim,
    BigDecimal valor,
    String status
) {}
