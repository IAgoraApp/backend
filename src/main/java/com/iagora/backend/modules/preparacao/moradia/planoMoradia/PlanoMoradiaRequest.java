package com.iagora.backend.modules.preparacao.moradia.planoMoradia;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PlanoMoradiaRequest(
    String nome,
    BigDecimal rendaMensal,
    BigDecimal reservaAtual,
    LocalDate mudancaPrevista,
    Integer casaDesejadaId,
    Integer userId
) {}
