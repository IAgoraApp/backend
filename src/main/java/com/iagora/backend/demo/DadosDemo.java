package com.iagora.backend.demo;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("demo & !prod & !production")
@ConditionalOnProperty(name = "iagora.demo.enabled", havingValue = "true")
public class DadosDemo implements ApplicationRunner {
    private final CatalogoDemo catalogo;
    private final ContasDemo contas;
    private final String senhaA;
    private final String senhaB;
    private final String senhaC;

    public DadosDemo(CatalogoDemo catalogo, ContasDemo contas,
            @Value("${IAGORA_DEMO_SENHA_A:}") String senhaA,
            @Value("${IAGORA_DEMO_SENHA_B:}") String senhaB,
            @Value("${IAGORA_DEMO_SENHA_C:}") String senhaC) {
        this.catalogo = catalogo;
        this.contas = contas;
        this.senhaA = senhaA;
        this.senhaB = senhaB;
        this.senhaC = senhaC;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        catalogo.popular();
        contas.popular(senhaA, senhaB, senhaC);
    }
}
