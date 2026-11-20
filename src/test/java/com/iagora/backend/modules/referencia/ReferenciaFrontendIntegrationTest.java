package com.iagora.backend.modules.referencia;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.iagora.backend.modules.identidade.autenticacao.User;
import com.iagora.backend.modules.identidade.autenticacao.UserRepository;
import com.iagora.backend.modules.identidade.perfil.Perfil;
import com.iagora.backend.modules.identidade.perfil.PerfilRepository;
import com.iagora.backend.security.JwtService;
import com.jayway.jsonpath.JsonPath;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@SpringBootTest(properties = {
    "spring.datasource.url=${iagora.test.db.url:jdbc:h2:mem:referencia;MODE=MySQL;DB_CLOSE_DELAY=-1}",
    "spring.datasource.driver-class-name=${iagora.test.db.driver:org.h2.Driver}",
    "spring.datasource.username=${iagora.test.db.user:sa}",
    "spring.datasource.password=${iagora.test.db.password:}",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.mail.username=teste",
    "spring.mail.password=teste",
    "jwt.secret=MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE=",
    "security.jwt.enabled=true",
    "GOOGLE_CLIENT_ID=iagora-test.apps.googleusercontent.com",
    "iagora.administracao.usuarios=90001",
    "iagora.pagamentos.teste=true",
    "iagora.pagamentos.webhook.segredo=MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE="
})
@AutoConfigureMockMvc
class ReferenciaFrontendIntegrationTest {

    @Autowired private JdbcTemplate jdbc;
    @Autowired private MockMvc mvc;
    @Autowired private UserRepository users;
    @Autowired private PerfilRepository perfis;
    @Autowired private JwtService jwt;
    @Autowired private com.iagora.backend.modules.identidade.resetSenha.TokenResetService recuperacao;

    private int cliente;
    private int empresa;
    private int externo;
    private static final int ADMIN = 90001;
    private static final String SEGREDO_WEBHOOK = "MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE=";

    @BeforeEach
    void contas() {
        if (!users.existsById(ADMIN)) {
            jdbc.update("insert into users (id, email, senha, tipo) values (?, ?, ?, ?)", ADMIN,
                "admin@referencia.test", "hash", 1);
        }
        cliente = conta(1);
        empresa = conta(2);
        externo = conta(1);
        perfis.save(new Perfil(users.getReferenceById(cliente), "Cliente", 1));
    }

    @Autowired private com.iagora.backend.modules.identidade.autenticacao.LoginGoogleService google;

    @Test
    void googleCriaContaUmaVezEAbreOnboarding() throws Exception {
        var decoder = org.mockito.Mockito.mock(org.springframework.security.oauth2.jwt.NimbusJwtDecoder.class);
        var anterior = org.springframework.test.util.ReflectionTestUtils.getField(google, "decoder");
        org.springframework.test.util.ReflectionTestUtils.setField(google, "decoder", decoder);
        try {
            String email = UUID.randomUUID() + "@google.example";
            var identidade = org.springframework.security.oauth2.jwt.Jwt.withTokenValue("credencial-teste")
                .header("alg", "RS256").subject(UUID.randomUUID().toString())
                .issuer("https://accounts.google.com").audience(java.util.List.of("iagora-test.apps.googleusercontent.com"))
                .expiresAt(Instant.now().plusSeconds(300)).claim("email", email).claim("email_verified", true).build();
            org.mockito.Mockito.when(decoder.decode("credencial-teste")).thenReturn(identidade);
            String pedido = """
                {"credential":"credencial-teste"}
                """;
            String resposta = mvc.perform(post("/user/login/google").contentType("application/json").content(pedido))
                .andExpect(status().isOk()).andExpect(jsonPath("$.novaConta").value(true))
                .andExpect(jsonPath("$.tipo").value(1)).andExpect(jsonPath("$.token").isString())
                .andReturn().getResponse().getContentAsString();
            int id = JsonPath.read(resposta, "$.id");
            mvc.perform(auth(get("/perfis/user/" + id), id)).andExpect(status().isOk());
            mvc.perform(post("/user/login/google").contentType("application/json").content(pedido))
                .andExpect(status().isOk()).andExpect(jsonPath("$.novaConta").value(false))
                .andExpect(jsonPath("$.id").value(id));
        } finally {
            org.springframework.test.util.ReflectionTestUtils.setField(google, "decoder", anterior);
        }
    }

    @Test
    void openApiPublicaOsContratosDeIntegracao() throws Exception {
        String resposta = mvc.perform(get("/v3/api-docs")).andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        java.util.Map<String, Object> caminhos = JsonPath.read(resposta, "$.paths");
        org.assertj.core.api.Assertions.assertThat(caminhos).containsKeys("/casas/{id}/residencia",
            "/casas/{id}/endereco", "/planos-moradia/custos-mensais", "/jornadas/guia",
            "/servicos/{servicoId}/contratar-com-conversa", "/recorrencias-financeiras/{id}/gerar-mes",
            "/metodos-pagamento/teste", "/user/reset-password");
        String destino = System.getProperty("iagora.test.openapi");
        if (destino != null) java.nio.file.Files.writeString(java.nio.file.Path.of(destino), resposta);
    }

    @Test
    void recuperacaoInvalidaTokenAnteriorEAceitaUsoUnico() throws Exception {
        String senha = UUID.randomUUID().toString();
        String email = UUID.randomUUID() + "@integracao.example";
        String resposta = mvc.perform(post("/user/register").contentType("application/json").content("""
            {"email":"%s","senha":"%s","tipo":1}
            """.formatted(email, senha))).andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        int conta = JsonPath.read(resposta, "$.id");
        String anterior = recuperacao.criarToken(conta);
        String atual = recuperacao.criarToken(conta);
        String novaSenha = UUID.randomUUID().toString();
        String alteracao = """
            {"token":"%s","novaSenha":"%s"}
            """;
        mvc.perform(post("/user/reset-password").contentType("application/json").content(alteracao.formatted(anterior, novaSenha)))
            .andExpect(status().isBadRequest());
        mvc.perform(post("/user/reset-password").contentType("application/json").content(alteracao.formatted(atual, novaSenha)))
            .andExpect(status().isNoContent());
        mvc.perform(post("/user/reset-password").contentType("application/json").content(alteracao.formatted(atual, senha)))
            .andExpect(status().isBadRequest());
        mvc.perform(post("/user/login").contentType("application/json").content("""
            {"email":"%s","senha":"%s"}
            """.formatted(email, novaSenha))).andExpect(status().isOk()).andExpect(jsonPath("$.token").isString());
    }

    @Test
    void guiaCompletoReutilizaAContaEIsolaTarefas() throws Exception {
        String guia = """
            {"nome":"Guia de teste","descricao":"Primeira mudança","etapas":[
            {"etapa":{"titulo":"Organizar caixas","ordem":1},"tarefas":[{"titulo":"Separar documentos","ordem":1}]}]}
            """;
        int id = criar("/jornadas/guia", cliente, guia);
        org.assertj.core.api.Assertions.assertThat(criar("/jornadas/guia", cliente, guia)).isEqualTo(id);
        mvc.perform(auth(get("/jornadas/" + id + "/etapas"), externo)).andExpect(status().isForbidden());
        String etapas = mvc.perform(auth(get("/jornadas/" + id + "/etapas"), cliente))
            .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1))
            .andReturn().getResponse().getContentAsString();
        int etapa = JsonPath.read(etapas, "$[0].id");
        mvc.perform(auth(get("/etapas-jornada/" + etapa + "/tarefas"), cliente))
            .andExpect(status().isOk()).andExpect(jsonPath("$[0].titulo").value("Separar documentos"));
    }

    @Test
    void geraMesFinanceiroSemDuplicarEValidaCategoria() throws Exception {
        int categoria = criar("/categorias-financas", cliente, """
            {"nome":"Contas da casa"}
            """);
        int categoriaAlheia = criar("/categorias-financas", externo, """
            {"nome":"Categoria de outra pessoa"}
            """);
        LocalDate dia = LocalDate.now().plusMonths(2).withDayOfMonth(10);
        int regra = criar("/recorrencias-financeiras", cliente, """
            {"descricao":"Internet","tipo":"despesa","valorPrevisto":89,"frequencia":"mensal","inicioEm":"%s"}
            """.formatted(dia));
        String rota = "/recorrencias-financeiras/" + regra + "/gerar-mes";
        String mes = java.time.YearMonth.from(dia).toString();
        String pedido = """
            {"mes":"%s","categoriaId":%d}
            """;
        mvc.perform(json(post(rota), cliente, pedido.formatted(mes, categoriaAlheia))).andExpect(status().isForbidden());
        mvc.perform(json(post(rota), cliente, pedido.formatted(mes, categoria)))
            .andExpect(status().isOk()).andExpect(jsonPath("$.quantidade").value(1));
        mvc.perform(json(post(rota), cliente, pedido.formatted(mes, categoria)))
            .andExpect(status().isOk()).andExpect(jsonPath("$.quantidade").value(0));
        mvc.perform(auth(get("/transacoes-financeiras/usuario/" + cliente), cliente))
            .andExpect(jsonPath("$[0].valor").value(89)).andExpect(jsonPath("$[0].categoriaId").value(categoria));
    }

    @Test
    void receitaECompraComItensUsamUmaTransacao() throws Exception {
        String rotaReceita = "/receitas/user/" + cliente;
        String corpo = """
            {"userId":%d,"publica":false,"statusPublicacao":"publicada","nome":"Receita de teste",
             "preparo":"Misturar","ingredientes":[{"nomeItem":"Aveia","quantidade":30},{"quantidade":1}]}
            """;
        mvc.perform(json(post("/receitas"), cliente, corpo.formatted(cliente))).andExpect(status().isBadRequest());
        mvc.perform(auth(get(rotaReceita), cliente)).andExpect(jsonPath("$.length()").value(0));
        String receita = """
            {"userId":%d,"publica":false,"statusPublicacao":"publicada","nome":"Receita de teste",
             "preparo":"Misturar","ingredientes":[{"nomeItem":"Aveia","quantidade":30}]}
            """;
        int id = criar("/receitas", cliente, receita.formatted(cliente));
        mvc.perform(auth(get("/receitas/" + id + "/itens"), cliente)).andExpect(jsonPath("$[0].nomeItem").value("Aveia"));
        mvc.perform(auth(get("/receitas/" + id + "/itens"), externo)).andExpect(status().isForbidden());

        String cadastro = mvc.perform(post("/user/register").contentType("application/json").content("""
            {"email":"%s","senha":"%s","tipo":1}
            """.formatted(UUID.randomUUID() + "@integracao.example", UUID.randomUUID())))
            .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        int usuario = JsonPath.read(cadastro, "$.id");
        String casas = mvc.perform(auth(get("/casas/user/" + usuario), usuario)).andReturn().getResponse().getContentAsString();
        int casa = JsonPath.read(casas, "$[0].id");
        String lista = """
            {"casaId":%d,"nome":"Reposição","itens":[{"nomeItem":"Aveia","quantidade":1}]}
            """.formatted(casa);
        int compra = criar("/compras", usuario, lista);
        mvc.perform(auth(get("/compras/" + compra + "/itens"), usuario)).andExpect(jsonPath("$[0].nomeItem").value("Aveia"));
    }

    @Test
    void protegeEnderecoDaResidenciaELoteDeCustos() throws Exception {
        String email = UUID.randomUUID() + "@integracao.example";
        var cadastro = mvc.perform(post("/user/register").contentType("application/json").content("""
            {"email":"%s","senha":"%s","tipo":1}
            """.formatted(email, UUID.randomUUID()))).andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        int residente = JsonPath.read(cadastro, "$.id");
        var casas = mvc.perform(auth(get("/casas/user/" + residente), residente)).andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        int casa = JsonPath.read(casas, "$[0].id");
        String perfil = mvc.perform(auth(get("/perfis/user/" + residente), residente)).andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        int idPerfil = JsonPath.read(perfil, "$.id");
        String novoEmail = UUID.randomUUID() + "@integracao.example";
        String identidade = """
            {"nome":"Pessoa de teste","email":"%s"}
            """.formatted(novoEmail);
        mvc.perform(json(patch("/perfis/" + idPerfil + "/identidade"), externo, identidade))
            .andExpect(status().isForbidden());
        mvc.perform(json(patch("/perfis/" + idPerfil + "/identidade"), residente, identidade))
            .andExpect(status().isOk()).andExpect(jsonPath("$.email").value(novoEmail));
        String residencia = """
            {"nome":"Apartamento","tipo":"Apartamento","endereco":{"rua":"Rua Demo","bairro":"Centro",
            "estado":"SP","cidade":"São Paulo","cep":"01001-000","numero":"12"}}
            """;
        mvc.perform(json(patch("/casas/" + casa + "/residencia"), externo, residencia)).andExpect(status().isForbidden());
        mvc.perform(json(patch("/casas/" + casa + "/residencia"), residente, residencia)).andExpect(status().isNoContent());
        var endereco = mvc.perform(auth(get("/casas/" + casa + "/endereco"), residente))
            .andExpect(status().isOk()).andExpect(jsonPath("$.rua").value("Rua Demo"))
            .andReturn().getResponse().getContentAsString();
        int idEndereco = JsonPath.read(endereco, "$.id");
        mvc.perform(auth(get("/enderecos/" + idEndereco), externo)).andExpect(status().isForbidden());
        mvc.perform(auth(delete("/enderecos/" + idEndereco), externo)).andExpect(status().isForbidden());

        String detalhesExternos = mvc.perform(post("/user/register").contentType("application/json").content("""
            {"email":"%s","senha":"%s","tipo":1}
            """.formatted(UUID.randomUUID() + "@integracao.example", UUID.randomUUID())))
            .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        int outroResidente = JsonPath.read(detalhesExternos, "$.id");
        String outrasCasas = mvc.perform(auth(get("/casas/user/" + outroResidente), outroResidente))
            .andReturn().getResponse().getContentAsString();
        int outraCasa = JsonPath.read(outrasCasas, "$[0].id");
        mvc.perform(json(patch("/casas/" + outraCasa), outroResidente, """
            {"enderecoId":%d}
            """.formatted(idEndereco)))
            .andExpect(status().isForbidden());

        int imovel = criar("/casas-desejadas", cliente, """
            {"titulo":"Apartamento de teste","tipo":"Apartamento","valor":1000,"valorTipo":"mensal"}
            """);
        String custos = """
            {"casaDesejadaId":%d,"custos":[{"nome":"Internet","categoria":"servicos","valor":80}]}
            """.formatted(imovel);
        mvc.perform(json(put("/planos-moradia/custos-mensais"), externo, custos)).andExpect(status().isForbidden());
        mvc.perform(json(put("/planos-moradia/custos-mensais"), cliente, custos)).andExpect(status().isNoContent());
        mvc.perform(json(put("/planos-moradia/custos-mensais"), cliente, custos)).andExpect(status().isNoContent());
        var planos = mvc.perform(auth(get("/planos-moradia/user/" + cliente), cliente)).andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1)).andReturn().getResponse().getContentAsString();
        int plano = JsonPath.read(planos, "$[0].id");
        mvc.perform(auth(get("/planos-moradia/" + plano + "/custos"), cliente))
            .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].valor").value(80));
    }

    @Test
    void recorrenciaGeraExecucoesEProtegeVinculos() throws Exception {
        int categoria = criar("/categorias-tarefa", cliente, """
            {"nome":"Organização"}
            """);
        int tarefa = criar("/tarefas", cliente, """
            {"nome":"Organizar a casa","categoriaId":%d}
            """.formatted(categoria));
        mvc.perform(json(post("/tarefas"), externo, """
            {"nome":"Tarefa","categoriaId":%d}
            """.formatted(categoria))).andExpect(status().isForbidden());
        mvc.perform(json(patch("/tarefas/" + tarefa), cliente, """
            {"lembreteMinutos":15}
            """)).andExpect(status().isNoContent());
        mvc.perform(auth(get("/tarefas/" + tarefa), cliente))
            .andExpect(jsonPath("$.lembreteMinutos").value(15));
        long hoje = LocalDate.now().getDayOfWeek().getValue();
        String dia = new String[]{"segunda", "terca", "quarta", "quinta", "sexta", "sabado", "domingo"}[(int) hoje - 1];
        criar("/tarefas/" + tarefa + "/recorrencia", cliente, """
            {"frequencia":"semanal","diasSemana":["%s"],"inicio":"%s"}
            """.formatted(dia, LocalDate.now()));
        mvc.perform(auth(get("/tarefas/" + tarefa + "/recorrencia"), cliente))
            .andExpect(status().isOk()).andExpect(jsonPath("$.diasSemana[0]").value(dia));
        String resposta = mvc.perform(auth(get("/tarefas/" + tarefa + "/execucoes"), cliente))
            .andExpect(status().isOk()).andExpect(jsonPath("$[0].status").value("pendente"))
            .andReturn().getResponse().getContentAsString();
        int execucao = JsonPath.read(resposta, "$[0].id");
        mvc.perform(auth(patch("/execucoes-tarefa/" + execucao + "/concluir"), cliente))
            .andExpect(status().isNoContent());
        mvc.perform(auth(get("/execucoes-tarefa/" + execucao), cliente))
            .andExpect(jsonPath("$.status").value("concluida")).andExpect(jsonPath("$.concluidaEm").exists());
        mvc.perform(auth(delete("/categorias-tarefa/" + categoria), cliente)).andExpect(status().isConflict());
        mvc.perform(auth(delete("/tarefas/" + tarefa), cliente)).andExpect(status().isNoContent());
        mvc.perform(auth(get("/execucoes-tarefa/" + execucao), cliente)).andExpect(status().isNotFound());
    }

    @Test
    void saldoConsideraPagamentoERecorrenciaFinanceiraGeraLancamento() throws Exception {
        int categoria = criar("/categorias-financas", cliente, """
            {"nome":"Moradia"}
            """);
        mvc.perform(json(post("/categorias-financas"), cliente, """
            {"nome":"MORADIA"}
            """)).andExpect(status().isConflict());
        int conta = criar("/contas-financeiras", cliente, """
            {"nome":"Carteira","tipo":"corrente","saldoInicial":100}
            """);
        int transacao = criar("/transacoes-financeiras", cliente, """
            {"categoriaId":%d,"contaId":%d,"descricao":"Mercado","tipo":"despesa","valor":30,"competencia":"%s"}
            """.formatted(categoria, conta, LocalDate.now()));
        mvc.perform(auth(patch("/transacoes-financeiras/" + transacao + "/pagar"), cliente))
            .andExpect(status().isNoContent());
        mvc.perform(auth(get("/contas-financeiras/" + conta), cliente))
            .andExpect(jsonPath("$.saldoAtual").value(70));
        criar("/recorrencias-financeiras", cliente, """
            {"descricao":"Salário","tipo":"receita","valorPrevisto":500,"frequencia":"mensal","inicioEm":"%s"}
            """.formatted(LocalDate.now()));
        mvc.perform(auth(get("/transacoes-financeiras/usuario/" + cliente + "/resumo"), cliente))
            .andExpect(status().isOk()).andExpect(jsonPath("$.despesas").value(30))
            .andExpect(jsonPath("$.receitasPendentes").value(500));
        mvc.perform(auth(get("/transacoes-financeiras/usuario/" + cliente + "/resumo"), externo))
            .andExpect(status().isForbidden());
    }

    @Test
    void dinheiroDistingueOmissaoDeNuloEAlimentaResumoDoPlano() throws Exception {
        mvc.perform(json(patch("/meu-dinheiro"), cliente, """
            {"renda":1500,"reserva":200,"mudancaPrevista":"2027-01-20"}
            """)).andExpect(status().isNoContent());
        mvc.perform(json(patch("/meu-dinheiro"), cliente, """
            {"mudancaPrevista":null}
            """)).andExpect(status().isNoContent());
        mvc.perform(auth(get("/meu-dinheiro"), cliente)).andExpect(status().isOk())
            .andExpect(jsonPath("$.renda").value(1500)).andExpect(jsonPath("$.mudancaPrevista").isEmpty());
        int plano = criar("/planos-moradia", cliente, """
            {"nome":"Mudança","rendaMensal":1200}
            """);
        criar("/planos-moradia/" + plano + "/custos", cliente, """
            {"nome":"Internet","tipo":"mensal","categoria":"servicos","valor":80}
            """);
        criar("/meu-dinheiro/gastos", cliente, """
            {"nome":"Transporte","frequencia":"fixo","destino":"continua","valor":60}
            """);
        criar("/meu-dinheiro/gastos", cliente, """
            {"nome":"Conta atual","frequencia":"fixo","destino":"termina","valor":40}
            """);
        mvc.perform(auth(get("/planos-moradia/" + plano + "/resumo"), cliente))
            .andExpect(status().isOk()).andExpect(jsonPath("$.gastosFuturos").value(140))
            .andExpect(jsonPath("$.saldoMensal").value(1360));
    }

    @Test
    void quizResguardaGabaritoEFinalizaApenasParaODono() throws Exception {
        mvc.perform(json(post("/aulas"), externo, """
            {"titulo":"Educação financeira","categoria":"financas"}
            """)).andExpect(status().isForbidden());
        int aula = criar("/aulas", ADMIN, """
            {"titulo":"Educação financeira","categoria":"financas"}
            """);
        int quiz = criar("/aulas/" + aula + "/quizzes", ADMIN, """
            {"titulo":"Orçamento"}
            """);
        int questao = criar("/quizzes/" + quiz + "/questoes", ADMIN, """
            {"enunciado":"Qual opção representa uma despesa?"}
            """);
        int alternativa = criar("/questoes/" + questao + "/alternativas", ADMIN, """
            {"texto":"Aluguel","correta":true}
            """);
        mvc.perform(auth(get("/alternativas/" + alternativa), cliente))
            .andExpect(status().isOk()).andExpect(jsonPath("$.correta").doesNotExist());
        int tentativa = criar("/quizzes/" + quiz + "/tentativas", cliente, "{}");
        criar("/tentativas-quiz/" + tentativa + "/respostas", cliente,
            """
            {"questaoId":%d,"alternativaId":%d}
            """.formatted(questao, alternativa));
        mvc.perform(auth(post("/tentativas-quiz/" + tentativa + "/finalizar"), externo))
            .andExpect(status().isForbidden());
        mvc.perform(auth(post("/tentativas-quiz/" + tentativa + "/finalizar"), cliente))
            .andExpect(status().isOk()).andExpect(jsonPath("$.pontuacao").value(100));
        mvc.perform(auth(patch("/aulas/" + aula + "/concluir"), cliente)).andExpect(status().isNoContent());
        mvc.perform(auth(get("/aulas/" + aula + "/progresso"), cliente))
            .andExpect(status().isOk()).andExpect(jsonPath("$.concluida").value(true));
    }

    @Test
    void empresaPodeAbrirFichaComAvaliacoesLogoAposSalvarRascunho() throws Exception {
        criar("/empresas/perfil", empresa, """
            {"nome":"Empresa de rascunho","cnpj":"%014d","categoria":"Reparos",
             "telefone":"11900000000","emailComercial":"rascunho@referencia.test"}
            """.formatted(empresa));

        int id = criar("/servicos", empresa, """
            {"titulo":"Instalação de prateleira","descricao":"Instalação em domicílio",
             "categoria":"reparos","preco":80.50,"tipoPreco":"fixo",
             "disponibilidade":{"dias":["segunda"]},"fotos":[]}
            """);
        mvc.perform(auth(get("/empresas/meus-servicos"), empresa))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(id))
            .andExpect(jsonPath("$[0].statusPublicacao").value("rascunho"));
        mvc.perform(auth(get("/servicos/" + id + "/avaliacoes"), empresa))
            .andExpect(status().isOk()).andExpect(content().json("[]"));
        mvc.perform(auth(get("/servicos/" + id + "/avaliacoes"), cliente))
            .andExpect(status().isNotFound());
    }

    @Test
    void marketplacePublicaAposPagamentoEConfirmaPropostaDoOutroParticipante() throws Exception {
        int perfilEmpresa = criar("/empresas/perfil", empresa, """
            {"nome":"Empresa de teste","cnpj":"%014d","categoria":"Reparos","telefone":"11900000000",
             "emailComercial":"empresa@referencia.test","horarioAtendimento":{"segunda":"08:00"}}
            """.formatted(empresa));
        mvc.perform(auth(post("/empresas/meu-perfil/enviar-validacao"), empresa)).andExpect(status().isNoContent());
        mvc.perform(auth(patch("/admin/empresas/" + perfilEmpresa + "/aprovar"), ADMIN)).andExpect(status().isNoContent());
        int servico = criar("/servicos", empresa, """
            {"titulo":"Reparo","descricao":"Atendimento técnico","categoria":"reparos","preco":90,"tipoPreco":"fixo"}
            """);
        mvc.perform(auth(get("/servicos/" + servico), cliente)).andExpect(status().isNotFound());
        String arquivo = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+aSakAAAAASUVORK5CYII=";
        var upload = new MockMultipartFile("arquivo", "foto.png", "image/png", Base64.getDecoder().decode(arquivo));
        String resposta = mvc.perform(multipart("/servicos/" + servico + "/imagens").file(upload).header("Authorization", "Bearer " + jwt.gerarToken(empresa)))
            .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String foto = JsonPath.read(resposta, "$.url");
        mvc.perform(json(patch("/servicos/" + servico), empresa, """
            {"fotos":["%s"]}
            """.formatted(foto))).andExpect(status().isNoContent());
        mvc.perform(auth(get(foto), cliente)).andExpect(status().isNotFound());
        mvc.perform(auth(post("/servicos/" + servico + "/enviar-validacao"), empresa)).andExpect(status().isNoContent());
        mvc.perform(auth(patch("/admin/servicos/" + servico + "/aprovar"), ADMIN)).andExpect(status().isNoContent());
        int plano = criar("/planos-publicacao", ADMIN, """
            {"nome":"Plano %d","preco":20,"duracaoDias":30}
            """.formatted(servico));
        int publicacao = criar("/servicos/" + servico + "/publicacoes", empresa, """
            {"planoId":%d}
            """.formatted(plano));
        int pagamento = criar("/pagamentos", empresa, """
            {"publicacaoId":%d}
            """.formatted(publicacao));
        mvc.perform(auth(post("/pagamentos/" + pagamento + "/confirmar"), cliente)).andExpect(status().isForbidden());
        String cobranca = mvc.perform(auth(get("/pagamentos/" + pagamento), empresa))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String idExterno = JsonPath.read(cobranca, "$.transacaoExternaId");
        String evento = """
            {"transacaoExternaId":"%s","status":"pago","valor":20}
            """.formatted(idExterno);
        mvc.perform(post("/pagamentos/webhook").contentType("application/json").content(evento))
            .andExpect(status().isUnauthorized());
        mvc.perform(webhook(evento)).andExpect(status().isNoContent());
        mvc.perform(webhook(evento)).andExpect(status().isNoContent());
        mvc.perform(auth(post("/pagamentos/" + pagamento + "/confirmar"), empresa)).andExpect(status().isNoContent());
        mvc.perform(auth(get(foto), cliente)).andExpect(status().isOk()).andExpect(content().contentType("image/png"));
        for (int i = 0; i < 2; i++) {
            mvc.perform(auth(put("/servicos/" + servico + "/favorito"), cliente)).andExpect(status().isNoContent());
        }
        mvc.perform(auth(get("/servicos-favoritos"), cliente)).andExpect(jsonPath("$.length()").value(1));

        String pedido = mvc.perform(json(post("/servicos/" + servico + "/contratar-com-conversa"), cliente, """
            {"descricao":"Atendimento para meu imóvel","endereco":{"rua":"Rua Demo","bairro":"Centro",
              "cidade":"São Paulo","estado":"SP","cep":"01001-000"}}
            """)).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        int contratacao = JsonPath.read(pedido, "$.contratadoId");
        int chatCriado = JsonPath.read(pedido, "$.id");
        mvc.perform(auth(get("/contratacoes/" + contratacao), empresa))
            .andExpect(jsonPath("$.endereco.rua").value("Rua Demo"));
        mvc.perform(auth(get("/contratacoes/" + contratacao), externo)).andExpect(status().isForbidden());
        String propostas = "/contratacoes/" + contratacao + "/propostas";
        int proposta = criar(propostas, empresa, """
            {"dataAgendamento":"%s","valor":88}
            """.formatted(LocalDateTime.now().plusDays(2)));
        mvc.perform(auth(patch(propostas + "/" + proposta + "/confirmar"), empresa)).andExpect(status().isForbidden());
        mvc.perform(auth(get(propostas), externo)).andExpect(status().isForbidden());
        mvc.perform(auth(patch(propostas + "/" + proposta + "/confirmar"), cliente)).andExpect(status().isNoContent());
        mvc.perform(auth(get("/contratacoes/" + contratacao), cliente)).andExpect(jsonPath("$.status").value("aceito"))
            .andExpect(jsonPath("$.valor").value(88));
        int chat = criar("/chats-servico", cliente, """
            {"contratadoId":%d}
            """.formatted(contratacao));
        org.assertj.core.api.Assertions.assertThat(chat).isEqualTo(chatCriado);
        criar("/chats-servico/" + chat + "/mensagens", cliente, """
            {"mensagem":"Combinado"}
            """);
        mvc.perform(auth(get("/chats-servico/" + chat + "/mensagens/nao-lidas"), empresa))
            .andExpect(jsonPath("$.quantidade").value(1));
        mvc.perform(auth(patch("/chats-servico/" + chat + "/mensagens/lidas"), empresa)).andExpect(status().isNoContent());
        mvc.perform(auth(get("/chats-servico/" + chat + "/mensagens/nao-lidas"), empresa))
            .andExpect(jsonPath("$.quantidade").value(0));
    }

    private MockHttpServletRequestBuilder webhook(String corpo) throws Exception {
        String timestamp = Long.toString(Instant.now().getEpochSecond());
        var mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(Base64.getDecoder().decode(SEGREDO_WEBHOOK), "HmacSHA256"));
        String assinatura = Base64.getEncoder().encodeToString(
            mac.doFinal((timestamp + "." + corpo).getBytes(StandardCharsets.UTF_8)));
        return post("/pagamentos/webhook").contentType("application/json").content(corpo)
            .header("X-IAgora-Timestamp", timestamp).header("X-IAgora-Assinatura", assinatura);
    }

    private int conta(int tipo) {
        return users.save(new User(UUID.randomUUID() + "@referencia.test", "hash", tipo)).getId();
    }

    private int criar(String rota, int userId, String body) throws Exception {
        String resposta = mvc.perform(json(post(rota), userId, body)).andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        return JsonPath.read(resposta, "$.id");
    }

    private MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder req, int userId, String body) {
        return auth(req, userId).contentType("application/json").content(body);
    }

    private MockHttpServletRequestBuilder auth(MockHttpServletRequestBuilder req, int userId) {
        return req.header("Authorization", "Bearer " + jwt.gerarToken(userId));
    }
}
