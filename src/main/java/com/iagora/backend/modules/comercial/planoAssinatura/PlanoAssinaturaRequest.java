package com.iagora.backend.modules.comercial.planoAssinatura;

import java.math.BigDecimal;
import tools.jackson.databind.JsonNode;

public record PlanoAssinaturaRequest(
    String nome,
    String descricao,
    BigDecimal preco,
    JsonNode beneficios
) {}
