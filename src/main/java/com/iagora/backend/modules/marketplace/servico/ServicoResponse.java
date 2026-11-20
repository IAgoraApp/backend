package com.iagora.backend.modules.marketplace.servico;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import tools.jackson.databind.JsonNode;

public record ServicoResponse(
    Integer id,
    Integer empresaId,
    String titulo,
    String descricao,
    JsonNode fotos,
    String categoria,
    BigDecimal preco,
    String tipoPreco,
    Boolean ativo,
    JsonNode disponibilidade,
    Boolean validado,
    BigDecimal nota,
    String statusPublicacao,
    LocalDateTime publicadoEm,
    LocalDateTime expiraEm
) {}
