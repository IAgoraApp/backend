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

        int contratacao = criar("/servicos/" + servico + "/contratar", cliente, "{}");
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
