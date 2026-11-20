package com.iagora.backend.modules.preparacao.dinheiroPreparacao;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DinheiroPreparacaoResponse(BigDecimal renda, BigDecimal reserva, LocalDate mudancaPrevista) {}
