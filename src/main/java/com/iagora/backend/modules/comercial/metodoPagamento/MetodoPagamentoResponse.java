package com.iagora.backend.modules.comercial.metodoPagamento;

public record MetodoPagamentoResponse(
    Integer id,
    Integer userId,
    String tipo,
    String apelido,
    String bandeira,
    String ultimosDigitos,
    Integer validadeMes,
    Integer validadeAno,
    Boolean principal,
    Boolean ativo
) {}
