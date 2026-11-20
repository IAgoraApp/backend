package com.iagora.backend.demo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.iagora.backend.modules.identidade.autenticacao.AuthService;
import com.iagora.backend.modules.identidade.autenticacao.CadastroRequest;
import com.iagora.backend.modules.identidade.autenticacao.LoginRequest;
import com.iagora.backend.modules.identidade.autenticacao.LoginResponse;
import com.jayway.jsonpath.JsonPath;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:demo;MODE=MySQL;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "jwt.secret=MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE=",
    "iagora.demo.enabled=true"
})
@ActiveProfiles("demo")
@AutoConfigureMockMvc
class DadosDemoIntegrationTest {
    private static final String SENHA = UUID.randomUUID().toString();

    @Autowired private DadosDemo dados;
    @Autowired private AuthService auth;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private MockMvc mvc;

    @DynamicPropertySource
    static void configurar(DynamicPropertyRegistry propriedades) {
        for (String tipo : new String[]{"A", "B", "C"}) {
            propriedades.add("IAGORA_DEMO_SENHA_" + tipo, () -> SENHA);
        }
    }

    @Test
    void seedIdempotenteSeparaContasEInicializaSeusFluxos() throws Exception {
        var tabelas = new String[]{"users", "produto", "receitas", "receita_item", "aula", "quiz", "questao",
            "alternativa", "plano_publicacao", "plano_assinatura", "comodo", "gasto_preparacao", "servico"};
        var primeiraExecucao = java.util.Arrays.stream(tabelas).map(this::contagem).toList();
        assertThat(primeiraExecucao).containsExactly(3, 9, 4, 8, 6, 6, 6, 18, 1, 1, 2, 1, 1);
        dados.run(new DefaultApplicationArguments());
        assertThat(java.util.Arrays.stream(tabelas).map(this::contagem).toList()).isEqualTo(primeiraExecucao);

        var a = login(ContasDemo.EMAIL_A);
        var b = login(ContasDemo.EMAIL_B);
        var c = login(ContasDemo.EMAIL_C);
        String casas = mvc.perform(get("/casas/user/{id}", a.id()).header("Authorization", bearer(a)))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        int casa = JsonPath.read(casas, "$[0].id");
        mvc.perform(get("/comodos/casa/{id}", casa).header("Authorization", bearer(a)))
            .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2));
        mvc.perform(get("/comodos/casa/{id}", casa).header("Authorization", bearer(b)))
            .andExpect(status().isForbidden());
        mvc.perform(get("/meu-dinheiro/gastos").header("Authorization", bearer(b)))
            .andExpect(status().isOk()).andExpect(jsonPath("$[0].nome").value("Transporte mensal"));
        var nova = auth.cadastrar(new CadastroRequest("novo@iagora.example", SENHA, 1));
        mvc.perform(get("/meu-dinheiro/gastos").header("Authorization", bearer(login(nova.email()))))
            .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
        mvc.perform(get("/empresas/meus-servicos").header("Authorization", bearer(c)))
            .andExpect(status().isOk()).andExpect(jsonPath("$[0].statusPublicacao").value("rascunho"));
    }

    private int contagem(String tabela) {
        return jdbc.queryForObject("select count(*) from " + tabela, Integer.class);
    }

    private LoginResponse login(String email) {
        return auth.logar(new LoginRequest(email, SENHA));
    }

    private String bearer(LoginResponse login) {
        return "Bearer " + login.token();
    }
}
