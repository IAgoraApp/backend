package com.iagora.backend.modules.preparacao.moradia.simulacaoMoradia;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SimulacaoMoradiaResponse(
    Integer id,
    Integer planoId,
    String nome,
    BigDecimal rendaMensal,
    BigDecimal reserva,
    LocalDateTime criadaEm
) {}
