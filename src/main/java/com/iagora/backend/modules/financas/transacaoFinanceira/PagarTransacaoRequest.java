package com.iagora.backend.modules.financas.transacaoFinanceira;

import java.time.LocalDate;

public record PagarTransacaoRequest(
    LocalDate pagoEm
) {}
