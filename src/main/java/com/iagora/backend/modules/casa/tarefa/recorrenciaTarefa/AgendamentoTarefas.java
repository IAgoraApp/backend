package com.iagora.backend.modules.casa.tarefa.recorrenciaTarefa;

import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class AgendamentoTarefas {

    private static final Logger log = LoggerFactory.getLogger(AgendamentoTarefas.class);
    private final RecorrenciaTarefaRepository repository;
    private final RecorrenciaTarefaService service;

    public AgendamentoTarefas(RecorrenciaTarefaRepository repository, RecorrenciaTarefaService service) {
        this.repository = repository;
        this.service = service;
    }

    @Scheduled(cron = "0 5 0 * * *", zone = "America/Sao_Paulo")
    public void processar() {
        LocalDate hoje = LocalDate.now(java.time.ZoneId.of("America/Sao_Paulo"));
        for (var item : repository.findAll()) {
            try {
                service.gerarOcorrencias(item.getId(), hoje);
            } catch (RuntimeException ex) {
                log.error("Falha ao processar recorrência {}", item.getId(), ex);
            }
        }
    }
}
