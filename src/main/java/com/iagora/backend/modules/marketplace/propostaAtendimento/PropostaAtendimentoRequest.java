package com.iagora.backend.modules.marketplace.propostaAtendimento;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PropostaAtendimentoRequest(
    LocalDateTime dataAgendamento,
    BigDecimal valor
) {}
