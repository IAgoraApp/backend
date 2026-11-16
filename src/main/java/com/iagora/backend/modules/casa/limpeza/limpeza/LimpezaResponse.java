package com.iagora.backend.modules.casa.limpeza.limpeza;

import java.time.LocalDate;

public record LimpezaResponse(
    Integer id,
    Integer comodoId,
    String nome,
    String descricao,
    LocalDate ultimaVez,
    LocalDate proximaVez
) {}
