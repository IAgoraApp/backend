package com.iagora.backend.modules.comercial.planoPublicacao;

import java.math.BigDecimal;

public record PlanoPublicacaoRequest(
    String nome,
    String descricao,
    BigDecimal preco,
    Integer duracaoDias
) {}
