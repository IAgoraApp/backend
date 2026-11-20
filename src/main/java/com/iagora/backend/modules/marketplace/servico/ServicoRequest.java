package com.iagora.backend.modules.marketplace.servico;

import java.math.BigDecimal;
import tools.jackson.databind.JsonNode;

public record ServicoRequest(
    Integer empresaId,
    String titulo,
    String descricao,
    JsonNode fotos,
    String categoria,
    BigDecimal preco,
    String tipoPreco,
    JsonNode disponibilidade
) {}
