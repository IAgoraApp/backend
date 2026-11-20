package com.iagora.backend.modules.comercial.planoPublicacao;

import java.math.BigDecimal;

public record PlanoPublicacaoResponse(
    Integer id,
    String nome,
    String descricao,
    BigDecimal preco,
    Integer duracaoDias,
    Boolean ativo
) {}
