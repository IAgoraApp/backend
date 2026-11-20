package com.iagora.backend.modules.preparacao.moradia.simulacaoMoradia;

import java.math.BigDecimal;

public record ResumoSimulacaoMoradiaResponse(BigDecimal rendaMensal, BigDecimal reserva, BigDecimal custosMensais,
                                            BigDecimal custosIniciais, BigDecimal saldoMensal, BigDecimal reservaRestante) {}
