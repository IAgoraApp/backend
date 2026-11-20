package com.iagora.backend.modules.preparacao.jornada.etapaJornada;

import java.time.LocalDate;

public record EtapaJornadaRequest(
    String titulo,
    String descricao,
    String categoria,
    Integer ordem,
    LocalDate prazo,
    String foto
) {}
