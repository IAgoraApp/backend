package com.iagora.backend.modules.preparacao.jornada.jornada;

import com.iagora.backend.modules.preparacao.jornada.etapaJornada.EtapaJornadaRequest;
import com.iagora.backend.modules.preparacao.jornada.tarefaJornada.TarefaJornadaRequest;
import java.util.List;

public record GuiaJornadaRequest(String nome, String descricao, List<Etapa> etapas) {
    public record Etapa(EtapaJornadaRequest etapa, List<TarefaJornadaRequest> tarefas) {}
}
