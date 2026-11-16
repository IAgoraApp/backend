package com.iagora.backend.modules.casa.cozinha.produto;

import java.math.BigDecimal;

public record ProdutoResponse(
    Integer id,
    String nome,
    String marca,
    BigDecimal tamanho,
    String unidade,
    String imagem
) {}
