package com.iagora.backend.modules.preparacao.moradia.simulacaoMoradia;

import java.math.BigDecimal;

public record SimulacaoMoradiaRequest(
    String nome,
    BigDecimal rendaMensal,
    BigDecimal reserva
) {}
