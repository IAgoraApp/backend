package com.iagora.backend.modules.preparacao.moradia.casaDesejada;

import java.math.BigDecimal;

public record CasaDesejadaRequest(
    Integer enderecoId,
    String titulo,
    String descricao,
    String tipo,
    BigDecimal valor,
    String valorTipo,
    String mobilia,
    Integer userId
) {}
