package com.iagora.backend.modules.financas.recorrenciaFinanceira;

import java.time.YearMonth;

public record GerarMesRequest(YearMonth mes, Integer categoriaId) {}
