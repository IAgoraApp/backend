package com.iagora.backend.modules.financas.metaFinanceira;

import java.math.BigDecimal;

public record AporteMetaFinanceiraRequest(
    BigDecimal valor
) {}
