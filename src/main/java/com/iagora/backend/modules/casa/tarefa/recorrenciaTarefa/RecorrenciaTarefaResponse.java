package com.iagora.backend.modules.casa.tarefa.recorrenciaTarefa;

import java.time.LocalDate;
import tools.jackson.databind.JsonNode;

public record RecorrenciaTarefaResponse(
    Integer id,
    Integer tarefaId,
    String frequencia,
    Integer intervalo,
    JsonNode diasSemana,
    JsonNode diaMes,
    LocalDate inicio,
    LocalDate fim
) {}
