package com.iagora.backend.modules.marketplace.servicoContratado;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ServicoContratadoResponse(
    Integer id,
    Integer servicoId,
    Integer perfilId,
    Integer enderecoId,
    String descricao,
    LocalDateTime dataAgendamento,
    BigDecimal valor,
    String status,
    LocalDateTime dataContratacao,
    LocalDateTime dataConclusao
) {}
