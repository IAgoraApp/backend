package com.iagora.backend.modules.marketplace.propostaAtendimento;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PropostaAtendimentoResponse(
    Integer id,
    Integer contratadoId,
    Integer autorUserId,
    LocalDateTime dataAgendamento,
    BigDecimal valor,
    String status
) {}
