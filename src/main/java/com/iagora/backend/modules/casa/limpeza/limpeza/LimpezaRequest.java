package com.iagora.backend.modules.casa.limpeza.limpeza;

import java.time.LocalDate;

public record LimpezaRequest(
    Integer comodoId,
    String nome,
    String descricao,
    LocalDate proximaVez
) {}
