package com.iagora.backend.modules.comercial.pagamento;

import java.math.BigDecimal;

public record EventoPagamentoRequest(String transacaoExternaId, String status, BigDecimal valor) {}
