package com.iagora.backend.modules.assistente;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.iagora.backend.modules.identidade.autenticacao.User;
import com.iagora.backend.modules.identidade.autenticacao.UserRepository;
import com.iagora.backend.security.JwtService;
import com.jayway.jsonpath.JsonPath;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:assistente;MODE=MySQL;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.mail.username=teste",
    "spring.mail.password=teste",
    "jwt.secret=MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE=",
    "security.jwt.enabled=true"
})
@AutoConfigureMockMvc
class AssistenteIntegrationTest {

  @Autowired private MockMvc mvc;
  @Autowired private JwtService jwt;
  @Autowired private UserRepository usuarios;
  @MockitoBean private GeminiService gemini;

  @Test
  void consultaSomenteReceitasDaContaAutenticada() throws Exception {
    int titular = conta();
    int outro = conta();
    criarReceita(titular, "Receita da conta autenticada");
    criarReceita(outro, "Receita de outra conta");
    String pergunta = """
      {"mensagens":[{"papel":"usuario","texto":"Quais receitas salvei?"}]}
      """;
    mvc.perform(post("/assistente/conversas").contentType("application/json").content(pergunta))
      .andExpect(status().isUnauthorized());

    when(gemini.responder(any(), any())).thenReturn(new GeminiService.Resposta(
      "Consulte suas receitas", List.of(new GeminiService.Link("Cozinha", "/cozinha")), null, null, null));
    mvc.perform(auth(post("/assistente/conversas"), titular).contentType("application/json").content(pergunta))
      .andExpect(status().isOk()).andExpect(header().string("Cache-Control", "no-store"))
      .andExpect(jsonPath("$.links[0].rota").value("/cozinha"));

    var registros = ArgumentCaptor.forClass(Object.class);
    verify(gemini).responder(any(), registros.capture());
    assertThat(registros.getValue().toString()).contains("Receita da conta autenticada", "financas", "rotina", "despensa", "metasMudanca")
      .doesNotContain("Receita de outra conta", "Preparo privado");
  }

  @Test
  void confirmaTarefaComExecucaoEAcessoSomenteDoTitular() throws Exception {
    int titular = conta();
    int outro = conta();
    String categoria = mvc.perform(auth(post("/categorias-tarefa"), titular).contentType("application/json")
        .content("{\"nome\":\"Casa\"}"))
      .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
    int categoriaId = JsonPath.read(categoria, "$.id");
    String proposta = """
      {"nome":"Dividir a faxina","categoriaId":%d,"prazo":"2027-01-20T10:00:00"}
      """.formatted(categoriaId);
    mvc.perform(auth(post("/assistente/tarefas"), outro).contentType("application/json").content(proposta))
      .andExpect(status().isForbidden());
    String tarefa = mvc.perform(auth(post("/assistente/tarefas"), titular).contentType("application/json").content(proposta))
      .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
    int tarefaId = JsonPath.read(tarefa, "$.id");
    mvc.perform(auth(get("/tarefas/" + tarefaId + "/execucoes"), titular))
      .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
  }

  @Test
  void confirmaGastoPagoNaDataInformadaComCategoriaDaPropriaConta() throws Exception {
    int titular = conta();
    int outro = conta();
    String proposta = """
      {"nome":"Almoço","tipo":"despesa","valor":20,"data":"2026-11-19","pago":true}
      """;
    mvc.perform(post("/assistente/lancamentos").contentType("application/json").content(proposta))
      .andExpect(status().isUnauthorized());
    String criado = mvc.perform(auth(post("/assistente/lancamentos"), titular)
        .contentType("application/json").content(proposta))
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.userId").value(titular)).andExpect(jsonPath("$.valor").value(20.0))
      .andExpect(jsonPath("$.status").value("pago")).andExpect(jsonPath("$.pagoEm").value("2026-11-19"))
      .andExpect(jsonPath("$.competencia").value("2026-11-01"))
      .andReturn().getResponse().getContentAsString();
    int categoria = JsonPath.read(criado, "$.categoriaId");
    mvc.perform(auth(get("/categorias-financas/" + categoria), titular))
      .andExpect(status().isOk()).andExpect(jsonPath("$.nome").value("Outros"));
    mvc.perform(auth(post("/assistente/lancamentos"), titular)
        .contentType("application/json").content(proposta))
      .andExpect(status().isCreated()).andExpect(jsonPath("$.categoriaId").value(categoria));
    String comCategoriaAlheia = proposta.strip().replace("}", ",\"categoriaId\":" + categoria + "}");
    mvc.perform(auth(post("/assistente/lancamentos"), outro)
        .contentType("application/json").content(comCategoriaAlheia))
      .andExpect(status().isForbidden());
  }

  @Test
  void historicoPersisteEditaEExcluiSomenteNaContaAutenticada() throws Exception {
    int titular = conta();
    int outro = conta();
    String caminho = "/assistente/historico";
    String conversa = caminho + "/" + UUID.randomUUID();
    String pedido = """
      {"titulo":"Almoço","mensagens":[{"id":"m1","papel":"usuario","texto":"Gastei 20 reais"}]}
      """;
    mvc.perform(get(caminho)).andExpect(status().isUnauthorized());
    mvc.perform(auth(put(conversa), titular).contentType("application/json").content(pedido))
      .andExpect(status().isNoContent());
    mvc.perform(auth(get(caminho), outro))
      .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
    mvc.perform(auth(delete(conversa), outro)).andExpect(status().isNoContent());
    mvc.perform(auth(get(caminho), titular))
      .andExpect(status().isOk()).andExpect(header().string("Cache-Control", "no-store"))
      .andExpect(jsonPath("$[0].mensagens[0].texto").value("Gastei 20 reais"));
    mvc.perform(auth(put(conversa), titular).contentType("application/json")
        .content(pedido.replace("20 reais", "25 reais")))
      .andExpect(status().isNoContent());
    mvc.perform(auth(get(caminho), titular))
      .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1))
      .andExpect(jsonPath("$[0].mensagens[0].texto").value("Gastei 25 reais"));
    mvc.perform(auth(delete(conversa), titular)).andExpect(status().isNoContent());
    mvc.perform(auth(get(caminho), titular))
      .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
  }

  private int conta() {
    var usuario = new User();
    usuario.setEmail(UUID.randomUUID() + "@assistente.test");
    usuario.setSenha("hash");
    usuario.setTipo(1);
    return usuarios.save(usuario).getId();
  }

  private void criarReceita(int usuario, String nome) throws Exception {
    String receita = """
      {"userId":%d,"nome":"%s","preparo":"Preparo privado","publica":false,"statusPublicacao":"rascunho"}
      """.formatted(usuario, nome);
    mvc.perform(auth(post("/receitas"), usuario).contentType("application/json").content(receita))
      .andExpect(status().isCreated());
  }

  private MockHttpServletRequestBuilder auth(MockHttpServletRequestBuilder requisicao, int usuario) {
    return requisicao.header("Authorization", "Bearer " + jwt.gerarToken(usuario));
  }
}
