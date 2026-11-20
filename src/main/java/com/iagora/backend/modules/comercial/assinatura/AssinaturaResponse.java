package com.iagora.backend.modules.comercial.assinatura;

import java.time.LocalDateTime;

public record AssinaturaResponse(
    Integer id,
    Integer userId,
    Integer planoId,
    String status,
    LocalDateTime inicio,
    LocalDateTime proximaCobranca,
    LocalDateTime canceladaEm
) {}
