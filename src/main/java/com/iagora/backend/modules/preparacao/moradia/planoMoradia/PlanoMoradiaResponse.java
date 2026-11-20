package com.iagora.backend.modules.preparacao.moradia.planoMoradia;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PlanoMoradiaResponse(
    Integer id,
    Integer userId,
    String nome,
    BigDecimal rendaMensal,
    BigDecimal reservaAtual,
    LocalDate mudancaPrevista,
    String status,
    Integer casaDesejadaId
) {}
