package com.iagora.backend.modules.financas.recorrenciaFinanceira;

import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class AgendamentoFinanceiro {

    private static final Logger log = LoggerFactory.getLogger(AgendamentoFinanceiro.class);
    private final RecorrenciaFinanceiraRepository repository;
    private final RecorrenciaFinanceiraService service;

    public AgendamentoFinanceiro(RecorrenciaFinanceiraRepository repository, RecorrenciaFinanceiraService service) {
        this.repository = repository;
        this.service = service;
    }

    @Scheduled(cron = "0 5 0 * * *", zone = "America/Sao_Paulo")
    public void processar() {
        LocalDate hoje = LocalDate.now(java.time.ZoneId.of("America/Sao_Paulo"));
        for (var item : repository.findByAtivaTrueOrderByIdAsc()) {
            try {
                service.gerarLancamentos(item.getId(), hoje);
            } catch (RuntimeException ex) {
                log.error("Falha ao processar recorrência {}", item.getId(), ex);
            }
        }
    }
}
