package com.iagora.backend.modules.preparacao.jornada.etapaJornada;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record EtapaJornadaResponse(
    Integer id,
    Integer jornadaId,
    String titulo,
    String descricao,
    String categoria,
    Integer ordem,
    String status,
    LocalDate prazo,
    LocalDateTime concluidaEm,
    String foto
) {}
