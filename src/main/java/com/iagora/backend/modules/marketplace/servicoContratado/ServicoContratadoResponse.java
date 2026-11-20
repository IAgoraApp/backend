package com.iagora.backend.modules.marketplace.servicoContratado;

import com.iagora.backend.modules.endereco.EnderecoResponse;
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
    LocalDateTime dataConclusao,
    EnderecoResponse endereco
) {}
