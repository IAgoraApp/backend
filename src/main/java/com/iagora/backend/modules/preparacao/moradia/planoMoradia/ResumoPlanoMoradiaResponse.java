package com.iagora.backend.modules.preparacao.moradia.planoMoradia;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ResumoPlanoMoradiaResponse(
    BigDecimal renda,
    BigDecimal reserva,
    LocalDate mudancaPrevista,
    BigDecimal custosMensais,
    BigDecimal custosIniciais,
    BigDecimal gastosMantidos,
    BigDecimal gastosFuturos,
    BigDecimal saldoMensal,
    BigDecimal reservaRestante
) {}
