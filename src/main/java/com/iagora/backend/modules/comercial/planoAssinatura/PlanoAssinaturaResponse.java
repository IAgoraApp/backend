package com.iagora.backend.modules.comercial.planoAssinatura;

import java.math.BigDecimal;
import tools.jackson.databind.JsonNode;

public record PlanoAssinaturaResponse(
    Integer id,
    String nome,
    String descricao,
    BigDecimal preco,
    JsonNode beneficios,
    Boolean ativo
) {}
