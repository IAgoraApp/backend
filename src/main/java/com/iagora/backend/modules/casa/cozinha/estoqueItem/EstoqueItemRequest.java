package com.iagora.backend.modules.casa.cozinha.estoqueItem;

import java.math.BigDecimal;

public record EstoqueItemRequest(
    Integer casaId,
    Integer produtoId,
    BigDecimal quantidade
) {}
