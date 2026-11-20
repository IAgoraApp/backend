package com.iagora.backend.modules.marketplace.servicoContratado;

import com.iagora.backend.modules.endereco.EnderecoRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ServicoContratadoRequest(
    Integer enderecoId,
    String descricao,
    LocalDateTime dataAgendamento,
    BigDecimal valor,
    EnderecoRequest endereco
) {}
