package com.iagora.backend.modules.marketplace.avaliacaoServico;

import java.time.LocalDateTime;

public record AvaliacaoServicoResponse(
    Integer id,
    Integer contratadoId,
    Integer perfilId,
    Integer nota,
    String comentario,
    LocalDateTime dataAvaliacao
) {}
