package com.iagora.backend.modules.casa.cozinha.compraItem;

import java.math.BigDecimal;

public record CompraItemRequest(
    Integer produtoId,
    String nomeItem,
    Integer quantidade,
    BigDecimal valor
) {}
