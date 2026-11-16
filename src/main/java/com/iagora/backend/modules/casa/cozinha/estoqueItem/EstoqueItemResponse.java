package com.iagora.backend.modules.casa.cozinha.estoqueItem;

import java.math.BigDecimal;

public record EstoqueItemResponse(
    Integer id,
    Integer casaId,
    Integer produtoId,
    BigDecimal quantidade
) {}
