package com.iagora.backend.modules.casa.cozinha.receitaItem;

import java.math.BigDecimal;

public record ReceitaItemRequest(
    Integer produtoId,
    String nomeItem,
    BigDecimal quantidade
) {}
