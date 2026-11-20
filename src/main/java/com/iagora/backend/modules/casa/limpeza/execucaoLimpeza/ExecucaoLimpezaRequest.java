package com.iagora.backend.modules.casa.limpeza.execucaoLimpeza;

import java.time.LocalDateTime;

public record ExecucaoLimpezaRequest(
    Integer perfilId,
    LocalDateTime realizadaEm
) {}
