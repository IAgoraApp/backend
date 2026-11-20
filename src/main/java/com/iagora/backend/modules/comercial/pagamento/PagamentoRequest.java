package com.iagora.backend.modules.comercial.pagamento;

public record PagamentoRequest(
    Integer metodoId,
    Integer assinaturaId,
    Integer publicacaoId
) {}
