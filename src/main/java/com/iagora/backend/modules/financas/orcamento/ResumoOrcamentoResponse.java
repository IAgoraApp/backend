package com.iagora.backend.modules.financas.orcamento;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ResumoOrcamentoResponse(Integer id, Integer categoriaId, LocalDate mes,
                                     BigDecimal limite, BigDecimal gasto, BigDecimal restante) {}
