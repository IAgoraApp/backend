package com.iagora.backend.modules.casa.cozinha.receitaItem;

import java.math.BigDecimal;

public record ReceitaItemResponse(
    Integer id,
    Integer receitaId,
    Integer produtoId,
    String nomeItem,
    BigDecimal quantidade
) {}
