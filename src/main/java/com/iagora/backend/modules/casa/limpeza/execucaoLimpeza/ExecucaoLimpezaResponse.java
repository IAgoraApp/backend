package com.iagora.backend.modules.casa.limpeza.execucaoLimpeza;

import java.time.LocalDateTime;

public record ExecucaoLimpezaResponse(
    Integer id,
    Integer limpezaId,
    Integer perfilId,
    LocalDateTime realizadaEm
) {}
