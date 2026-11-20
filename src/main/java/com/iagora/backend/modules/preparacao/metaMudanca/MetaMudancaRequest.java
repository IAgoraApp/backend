package com.iagora.backend.modules.preparacao.metaMudanca;

import java.math.BigDecimal;
import java.time.LocalDate;
import tools.jackson.databind.JsonNode;

public record MetaMudancaRequest(
    String nome,
    String fonte,
    BigDecimal alvo,
    String modo,
    LocalDate data,
    BigDecimal aporte,
    JsonNode estimativaMudanca,
    Integer userId
) {}
