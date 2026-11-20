package com.iagora.backend.modules.comercial.metodoPagamento;

public record MetodoPagamentoRequest(
    String tipo,
    String apelido,
    String bandeira,
    String ultimosDigitos,
    String tokenGateway,
    Integer validadeMes,
    Integer validadeAno
) {}
