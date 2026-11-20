package com.iagora.backend.modules.marketplace.servicoContratado;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ServicoContratadoRequest(
    Integer enderecoId,
    String descricao,
    LocalDateTime dataAgendamento,
    BigDecimal valor
) {}
