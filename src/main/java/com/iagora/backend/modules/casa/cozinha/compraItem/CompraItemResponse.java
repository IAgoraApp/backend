package com.iagora.backend.modules.casa.cozinha.compraItem;

import java.math.BigDecimal;

public record CompraItemResponse(
    Integer id,
    Integer comprasId,
    Integer produtoId,
    String nomeItem,
    Integer quantidade,
    Boolean comprado,
    BigDecimal valor
) {}
