package com.iagora.backend.modules.cozinha;

import com.iagora.backend.modules.catalogo.ImportadorCatalogo;
import com.iagora.backend.modules.casa.casa.Casa;
import com.iagora.backend.modules.casa.casa.CasaRepository;
import com.iagora.backend.modules.casa.cozinha.receita.ReceitaRequest;
import com.iagora.backend.modules.casa.cozinha.receita.ReceitaService;
import com.iagora.backend.modules.identidade.autenticacao.User;
import com.iagora.backend.modules.identidade.autenticacao.UserRepository;
import com.iagora.backend.security.JwtService;
import com.jayway.jsonpath.JsonPath;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.Base64;
import java.util.UUID;
import org.springframework.http.HttpMethod;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
  "spring.datasource.url=jdbc:h2:mem:cozinha;MODE=MySQL;DB_CLOSE_DELAY=-1",
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
@Transactional
class CozinhaIntegrationTest {

  @Autowired private MockMvc mvc;
  @Autowired private ImportadorCatalogo importador;
  @Autowired private JdbcTemplate jdbc;
  @Autowired private UserRepository userRepository;
  @Autowired private CasaRepository casaRepository;
  @Autowired private JwtService jwtService;
  @Autowired private ReceitaService receitaService;
  @Autowired private EntityManager entityManager;

  private User user;
  private User outro;
  private Casa casa;
  private Casa outraCasa;
  private String token;
  private String outroToken;

  @BeforeEach
  void configurar() {
    user = userRepository.save(new User(UUID.randomUUID() + "@teste.com", "hash", 1));
    outro = userRepository.save(new User(UUID.randomUUID() + "@teste.com", "hash", 1));
    casa = casaRepository.save(new Casa(user, "Minha casa", "Casa"));
    outraCasa = casaRepository.save(new Casa(outro, "Outra casa", "Casa"));
    token = jwtService.gerarToken(user.getId());
    outroToken = jwtService.gerarToken(outro.getId());
  }

  @Test
  void importaCatalogoCompletoSemDuplicarNemAlterarDadosPrivados() throws Exception {
    int privada = receita(false);
    var tabelas = java.util.Map.of(
      "produto", 500, "receitas", 50, "receita_item", 215, "aula", 10,
      "quiz", 10, "questao", 30, "alternativa", 90, "catalogo_importado", 1);
    var contagens = new java.util.HashMap<String, Integer>();
    tabelas.keySet().forEach(tabela -> contagens.put(tabela, contar(tabela)));

    importador.importar();
    entityManager.flush();
    importador.importar();
    entityManager.flush();

    tabelas.forEach((tabela, esperado) -> assertEquals(esperado.intValue(),
      contar(tabela) - contagens.get(tabela), tabela));
    assertEquals(9, jdbc.queryForObject(
      "select count(*) from receita_item where nome_item = ? and produto_id is null",
      Integer.class, "Sal refinado (g)"));
    mvc.perform(auth(get("/receitas/" + privada))).andExpect(jsonPath("$.publica").value(false));
    mvc.perform(get("/receitas")).andExpect(jsonPath("$[*].nome").value(hasItem("Arroz branco soltinho")));
  }

  private int contar(String tabela) {
    return jdbc.queryForObject("select count(*) from " + tabela, Integer.class);
  }

  @Test
  void produtoCompartilhadoNaoCriaEstoqueEPermiteBusca() throws Exception {
    int produto = produto("Arroz");
    mvc.perform(auth(get("/produtos/busca").param("nome", "aRR")))
      .andExpect(status().isOk()).andExpect(jsonPath("$[*].id").value(hasItem(produto)));
    mvc.perform(auth(get("/estoque-itens/casa/" + casa.getId())))
      .andExpect(status().isOk()).andExpect(content().json("[]"));
    mvc.perform(auth(get("/produtos"), outroToken))
      .andExpect(status().isOk()).andExpect(jsonPath("$[*].id").value(hasItem(produto)));
    mvc.perform(auth(patch("/produtos/" + produto).contentType("application/json")
      .content("""
        {"marca":"Alteração indevida"}
        """), outroToken)).andExpect(status().isForbidden());
    mvc.perform(auth(patch("/produtos/" + produto).contentType("application/json")
      .content("{\"marca\":\"Marca nova\"}"))).andExpect(status().isNoContent());
    mvc.perform(auth(get("/produtos/" + produto)))
      .andExpect(jsonPath("$.marca").value("Marca nova"));
    mvc.perform(auth(delete("/produtos/" + produto))).andExpect(status().isNoContent());
  }

  @Test
  void listaComecaVaziaEItensCompradosNaoAlteramEstoque() throws Exception {
    int produto = produto("Feijão");
    int compra = compra(casa, "Mercado");
    mvc.perform(auth(get("/compras/" + compra)))
      .andExpect(jsonPath("$.criacao").value(LocalDate.now().toString()));
    mvc.perform(auth(get("/compras/" + compra + "/itens")))
      .andExpect(content().json("[]"));
    int item = criar("/compras/" + compra + "/itens", "{\"produtoId\":" + produto + "}");
    mvc.perform(auth(get("/compras/" + compra + "/itens/" + item)))
      .andExpect(jsonPath("$.nomeItem").value("Feijão"))
      .andExpect(jsonPath("$.quantidade").value(1))
      .andExpect(jsonPath("$.comprado").value(false));
    mvc.perform(auth(patch("/compras/" + compra + "/itens/" + item + "/comprado")
      .contentType("application/json").content("{\"comprado\":true}")))
      .andExpect(status().isNoContent());
    mvc.perform(auth(get("/estoque-itens/casa/" + casa.getId())))
      .andExpect(content().json("[]"));
    mvc.perform(auth(patch("/compras/" + compra + "/itens/" + item + "/comprado")
      .contentType("application/json").content("{\"comprado\":false}")))
      .andExpect(status().isNoContent());
    int manual = criar("/compras/" + compra + "/itens", "{\"nomeItem\":\"Pão da padaria\"}");
    mvc.perform(auth(patch("/compras/" + compra + "/itens/" + manual)
      .contentType("application/json").content("{\"quantidade\":3,\"valor\":5.50}")))
      .andExpect(status().isNoContent());
    mvc.perform(auth(get("/compras/casa/" + casa.getId())))
      .andExpect(jsonPath("$[0].id").value(compra));
    mvc.perform(auth(delete("/compras/" + compra))).andExpect(status().isNoContent());
    entityManager.flush();
    mvc.perform(auth(get("/produtos/" + produto))).andExpect(status().isOk());
  }

  @Test
  void estoqueAtualizaMesmoRegistroEImpedeExcluirProdutoUtilizado() throws Exception {
    int produto = produto("Leite");
    String body = "{\"casaId\":%d,\"produtoId\":%d,\"quantidade\":%s}";
    int item = criar("/estoque-itens", body.formatted(casa.getId(), produto, "2.5"));
    int mesmo = criar("/estoque-itens", body.formatted(casa.getId(), produto, "7"));
    assertEquals(item, mesmo);
    mvc.perform(auth(get("/estoque-itens/casa/" + casa.getId())))
      .andExpect(jsonPath("$.length()").value(1))
      .andExpect(jsonPath("$[0].quantidade").value(7));
    mvc.perform(auth(delete("/produtos/" + produto))).andExpect(status().isConflict());
    mvc.perform(auth(patch("/estoque-itens/" + item).contentType("application/json")
      .content("{\"quantidade\":0}"))).andExpect(status().isNoContent());
    mvc.perform(auth(delete("/estoque-itens/" + item))).andExpect(status().isNoContent());
    mvc.perform(auth(get("/produtos/" + produto))).andExpect(status().isOk());
  }

  @Test
  void protegeDadosDaCasaEDaListaDeOutroUsuario() throws Exception {
    int compra = compra(casa, "Privada");
    int produto = produto("Sal");
    int estoque = criar("/estoque-itens",
      "{\"casaId\":%d,\"produtoId\":%d}".formatted(casa.getId(), produto));
    mvc.perform(auth(get("/compras/" + compra), outroToken)).andExpect(status().isForbidden());
    mvc.perform(auth(get("/compras/" + compra + "/itens"), outroToken)).andExpect(status().isForbidden());
    mvc.perform(auth(get("/estoque-itens/" + estoque), outroToken)).andExpect(status().isForbidden());
    mvc.perform(auth(post("/compras").contentType("application/json")
      .content("{\"casaId\":%d,\"nome\":\"Invadida\"}".formatted(outraCasa.getId()))))
      .andExpect(status().isForbidden());
    mvc.perform(auth(post("/estoque-itens").contentType("application/json")
      .content("{\"casaId\":%d,\"produtoId\":%d}".formatted(outraCasa.getId(), produto))))
      .andExpect(status().isForbidden());
  }

  @Test
  void itemNaoPodeSerAcessadoUsandoOutraLista() throws Exception {
    int compra = compra(casa, "Primeira");
    int segunda = compra(casa, "Segunda");
    int item = criar("/compras/" + compra + "/itens", "{\"nomeItem\":\"Pão\"}");
    mvc.perform(auth(get("/compras/" + segunda + "/itens/" + item)))
      .andExpect(status().isNotFound());
    mvc.perform(auth(delete("/compras/" + segunda + "/itens/" + item)))
      .andExpect(status().isNotFound());
  }

  @Test
  void receitasPrivadasSoAparecemParaAutor() throws Exception {
    int receita = receita(false);
    mvc.perform(get("/receitas"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$[*].nome").value(hasItem("Arroz integral com tomate")))
      .andExpect(jsonPath("$[*].id").value(not(hasItem(receita))));
    mvc.perform(get("/receitas/" + receita)).andExpect(status().isUnauthorized());
    mvc.perform(auth(get("/receitas/" + receita), outroToken)).andExpect(status().isForbidden());
    mvc.perform(auth(get("/receitas/" + receita)))
      .andExpect(status().isOk()).andExpect(jsonPath("$.status").doesNotExist());
    mvc.perform(auth(get("/receitas/user/" + user.getId()), outroToken))
      .andExpect(status().isForbidden());
    mvc.perform(auth(get("/receitas/user/" + user.getId())))
      .andExpect(jsonPath("$[0].id").value(receita));
    mvc.perform(auth(patch("/receitas/" + receita).contentType("application/json")
      .content("{\"publica\":true}")))
      .andExpect(status().isNoContent());
    mvc.perform(get("/receitas/" + receita)).andExpect(status().isOk());
    mvc.perform(get("/receitas")).andExpect(jsonPath("$[*].id").value(hasItem(receita)));
    mvc.perform(auth(patch("/receitas/" + receita).contentType("application/json")
      .content("{\"nome\":\"Outro nome\"}"), outroToken)).andExpect(status().isForbidden());
  }

  @Test
  void imagemDaReceitaPodeSerEnviadaETemMesmaVisibilidadeDaReceita() throws Exception {
    byte[] png = Base64.getDecoder().decode(
      "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+aSakAAAAASUVORK5CYII=");
    var arquivo = new MockMultipartFile("arquivo", "foto.png", "image/png", png);
    var dados = new MockMultipartFile("receita", "", "application/json", ("""
      {"userId":%d,"publica":false,"nome":"Receita com foto","preparo":"Misture"}
      """).formatted(user.getId()).getBytes(java.nio.charset.StandardCharsets.UTF_8));
    var criacao = mvc.perform(multipart("/receitas").file(dados).file(arquivo).header("Authorization", "Bearer " + token))
      .andExpect(status().isCreated()).andReturn();
    int id = JsonPath.read(criacao.getResponse().getContentAsString(), "$.id");
    entityManager.flush();
    String caminho = "/receitas/" + id;
    String imagem = caminho + "/imagem";
    mvc.perform(auth(get(caminho))).andExpect(jsonPath("$.imagem").value(imagem));
    mvc.perform(auth(get(imagem), outroToken)).andExpect(status().isForbidden());
    mvc.perform(auth(get(imagem))).andExpect(status().isOk())
      .andExpect(content().contentType("image/png")).andExpect(content().bytes(png));

    var patch = new MockMultipartFile("receita", "", "application/json", "{}".getBytes(java.nio.charset.StandardCharsets.UTF_8));
    mvc.perform(multipart(HttpMethod.PATCH, caminho).file(patch).file(arquivo).header("Authorization", "Bearer " + token))
      .andExpect(status().isNoContent());
    mvc.perform(auth(patch(caminho).contentType("application/json").content("{\"publica\":true}")))
      .andExpect(status().isNoContent());
    entityManager.flush();
    mvc.perform(get(imagem)).andExpect(status().isOk()).andExpect(content().bytes(png));
    mvc.perform(auth(patch(caminho).contentType("application/json").content("{\"imagem\":\"\"}")))
      .andExpect(status().isNoContent());
    mvc.perform(get(imagem)).andExpect(status().isNotFound());
  }

  @Test
  void ingredientesAceitamCatalogoOuNomeManualEProtegemReceitaPai() throws Exception {
    int receita = receita(true);
    int segunda = receita(true);
    int produto = produto("Ovos");
    int item = criar("/receitas/" + receita + "/itens",
      "{\"produtoId\":%d,\"quantidade\":2}".formatted(produto));
    int manual = criar("/receitas/" + receita + "/itens",
      "{\"nomeItem\":\"Sal a gosto\",\"quantidade\":1}");
    mvc.perform(get("/receitas/" + receita + "/itens/" + item))
      .andExpect(status().isOk()).andExpect(jsonPath("$.nomeItem").value("Ovos"));
    mvc.perform(get("/receitas/" + segunda + "/itens/" + item)).andExpect(status().isNotFound());
    mvc.perform(auth(patch("/receitas/" + receita + "/itens/" + item)
      .contentType("application/json").content("{\"quantidade\":3}"), outroToken))
      .andExpect(status().isForbidden());
    mvc.perform(auth(patch("/receitas/" + receita + "/itens/" + item)
      .contentType("application/json").content("{\"quantidade\":3}")))
      .andExpect(status().isNoContent());
    mvc.perform(auth(delete("/produtos/" + produto))).andExpect(status().isConflict());
    mvc.perform(auth(delete("/receitas/" + receita + "/itens/" + manual)))
      .andExpect(status().isNoContent());
    mvc.perform(auth(delete("/receitas/" + receita))).andExpect(status().isNoContent());
    entityManager.flush();
    mvc.perform(auth(get("/produtos/" + produto))).andExpect(status().isOk());
  }

  @Test
  void favoritosSaoPorUsuarioSemDuplicarEEscondemReceitasDespublicadas() throws Exception {
    int receita = receita(true);
    String path = "/receitas/" + receita + "/favoritos";
    int favorito = criar(path + "?userId=" + user.getId(), "{}");
    assertEquals(favorito, criar(path + "?userId=" + user.getId(), "{}"));
    mvc.perform(auth(get(path + "/status").param("userId", user.getId().toString())))
      .andExpect(jsonPath("$.favorita").value(true));
    mvc.perform(auth(get("/users/" + user.getId() + "/receitas-favoritas")))
      .andExpect(jsonPath("$.length()").value(1));
    mvc.perform(auth(get(path + "/status").param("userId", user.getId().toString()), outroToken))
      .andExpect(status().isForbidden());
    mvc.perform(auth(patch("/receitas/" + receita).contentType("application/json")
      .content("{\"publica\":false}"))).andExpect(status().isNoContent());
    mvc.perform(auth(get("/users/" + user.getId() + "/receitas-favoritas")))
      .andExpect(content().json("[]"));
    mvc.perform(auth(delete(path).param("userId", user.getId().toString())))
      .andExpect(status().isNoContent());
    mvc.perform(auth(get(path + "/status").param("userId", user.getId().toString())))
      .andExpect(jsonPath("$.favorita").value(false));
    mvc.perform(auth(post(path).param("userId", user.getId().toString())))
      .andExpect(status().isBadRequest());
  }

  @Test
  void receitasOficiaisSaoPublicasESemAutor() throws Exception {
    int receita = receitaService.cadastrar(new ReceitaRequest(null, true, "Oficial", null,
      null, "Misturar")).id();
    mvc.perform(get("/receitas/" + receita))
      .andExpect(status().isOk()).andExpect(jsonPath("$.userId").isEmpty());
    mvc.perform(auth(delete("/receitas/" + receita))).andExpect(status().isForbidden());
    mvc.perform(auth(post("/receitas").contentType("application/json")
      .content("{\"nome\":\"Oficial falsa\",\"preparo\":\"Misturar\"}")))
      .andExpect(status().isForbidden());
  }

  @Test
  void excluirUsuarioLimpaDependenciasEPreservaCatalogo() throws Exception {
    int produto = produto("Farinha");
    int compra = compra(casa, "Minha lista");
    criar("/compras/" + compra + "/itens", "{\"produtoId\":" + produto + "}");
    criar("/estoque-itens", "{\"casaId\":%d,\"produtoId\":%d}".formatted(casa.getId(), produto));
    int receita = receita(true);
    criar("/receitas/" + receita + "/itens", "{\"produtoId\":%d,\"quantidade\":1}".formatted(produto));
    criar("/receitas/" + receita + "/favoritos?userId=" + user.getId(), "{}");
    mvc.perform(auth(post("/receitas/" + receita + "/favoritos").param("userId", outro.getId().toString()), outroToken))
      .andExpect(status().isCreated());
    int oficial = receitaService.cadastrar(new ReceitaRequest(null, true, "Oficial", null, null, "Misturar")).id();
    criar("/receitas/" + oficial + "/favoritos?userId=" + user.getId(), "{}");
    mvc.perform(auth(delete("/user/" + user.getId()))).andExpect(status().isNoContent());
    entityManager.flush();
    entityManager.clear();
    assertFalse(userRepository.existsById(user.getId()));
    mvc.perform(auth(get("/produtos/" + produto), outroToken)).andExpect(status().isOk());
    mvc.perform(get("/receitas/" + oficial)).andExpect(status().isOk());
    mvc.perform(auth(get("/users/" + outro.getId() + "/receitas-favoritas"), outroToken))
      .andExpect(content().json("[]"));
  }

  @Test
  void rejeitaDadosEssenciaisInvalidos() throws Exception {
    mvc.perform(auth(post("/produtos").contentType("application/json").content("{\"nome\":\" \"}")))
      .andExpect(status().isBadRequest());
    int produto = produto("Água");
    mvc.perform(auth(post("/estoque-itens").contentType("application/json")
      .content("{\"casaId\":%d,\"produtoId\":%d,\"quantidade\":-1}".formatted(casa.getId(), produto))))
      .andExpect(status().isBadRequest());
    int compra = compra(casa, "Lista");
    for (String body : new String[] { "{}", "{\"nomeItem\":\"Pão\",\"quantidade\":0}",
      "{\"nomeItem\":\"Pão\",\"valor\":-1}" }) {
      mvc.perform(auth(post("/compras/" + compra + "/itens").contentType("application/json").content(body)))
        .andExpect(status().isBadRequest());
    }
    int receita = receita(false);
    mvc.perform(auth(post("/receitas/" + receita + "/itens").contentType("application/json")
      .content("{\"nomeItem\":\"Sal\"}"))).andExpect(status().isBadRequest());
    mvc.perform(auth(patch("/receitas/" + receita).contentType("application/json")
      .content("{\"nome\":\" \"}"))).andExpect(status().isBadRequest());
  }

  @Test
  void usuariosTemAndamentosIndependentesSemAlterarPublicacaoOuFavoritos() throws Exception {
    int receita = receita(true);
    String meu = "/users/" + user.getId() + "/receitas/" + receita;
    String dele = "/users/" + outro.getId() + "/receitas/" + receita;
    criar(meu, "{}");
    mvc.perform(auth(post(dele).contentType("application/json").content("{\"status\":\"fazendo\"}"), outroToken))
      .andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("fazendo"));
    mvc.perform(auth(get(meu))).andExpect(status().isOk())
      .andExpect(jsonPath("$.status").value("fazer"));
    for (String andamento : new String[] { "fazendo", "feita", "fazer" }) {
      mvc.perform(auth(patch(meu).contentType("application/json")
        .content("{\"status\":\"" + andamento + "\"}"))).andExpect(status().isNoContent());
      mvc.perform(auth(get(meu))).andExpect(jsonPath("$.status").value(andamento));
    }
    mvc.perform(auth(get(dele), outroToken)).andExpect(jsonPath("$.status").value("fazendo"));
    mvc.perform(get("/receitas/" + receita)).andExpect(status().isOk())
      .andExpect(jsonPath("$.publica").value(true))
      .andExpect(jsonPath("$.status").doesNotExist());
    mvc.perform(get("/receitas")).andExpect(jsonPath("$[*].id").value(hasItem(receita)));
    mvc.perform(auth(get("/receitas/" + receita + "/favoritos/status")
      .param("userId", user.getId().toString()))).andExpect(jsonPath("$.favorita").value(false));
    mvc.perform(auth(get("/users/" + user.getId() + "/receitas")))
      .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
  }

  @Test
  void protegeAndamentoPessoalEmTodasAsRotas() throws Exception {
    int receita = receita(true);
    String meu = "/users/" + user.getId() + "/receitas/" + receita;
    criar(meu, "{}");
    mvc.perform(auth(get(meu), outroToken)).andExpect(status().isForbidden());
    mvc.perform(auth(get("/users/" + user.getId() + "/receitas"), outroToken))
      .andExpect(status().isForbidden());
    mvc.perform(auth(patch(meu).contentType("application/json")
      .content("{\"status\":\"feita\"}"), outroToken)).andExpect(status().isForbidden());
    mvc.perform(auth(post(meu).contentType("application/json")
      .content("{}"), outroToken)).andExpect(status().isForbidden());
    mvc.perform(auth(delete(meu), outroToken)).andExpect(status().isForbidden());
    mvc.perform(get(meu)).andExpect(status().isUnauthorized());
  }

  @Test
  void naoAdicionaReceitaPrivadaDeOutroUsuarioMasPermiteDoAutor() throws Exception {
    int receita = receita(false);
    criar("/users/" + user.getId() + "/receitas/" + receita, "{}");
    mvc.perform(auth(post("/users/" + outro.getId() + "/receitas/" + receita)
      .contentType("application/json").content("{}"), outroToken))
      .andExpect(status().isForbidden());
  }

  @Test
  void receitaQueFicaPrivadaSaiDoAcompanhamentoDeOutrosUsuarios() throws Exception {
    int receita = receita(true);
    String path = "/users/" + outro.getId() + "/receitas/" + receita;
    mvc.perform(auth(post(path).contentType("application/json").content("{}"), outroToken))
      .andExpect(status().isCreated());
    mvc.perform(auth(patch("/receitas/" + receita).contentType("application/json")
      .content("{\"publica\":false}"))).andExpect(status().isNoContent());
    mvc.perform(auth(get(path), outroToken)).andExpect(status().isForbidden());
    mvc.perform(auth(get("/users/" + outro.getId() + "/receitas"), outroToken))
      .andExpect(status().isOk()).andExpect(content().json("[]"));
    mvc.perform(auth(patch(path).contentType("application/json")
      .content("{\"status\":\"feita\"}"), outroToken)).andExpect(status().isForbidden());
    mvc.perform(auth(delete(path), outroToken)).andExpect(status().isNoContent());
  }

  @Test
  void rejeitaAndamentoInvalidoEDuplicadoERetornaRegistroAusente() throws Exception {
    int receita = receita(true);
    String path = "/users/" + user.getId() + "/receitas/" + receita;
    mvc.perform(auth(get(path))).andExpect(status().isNotFound());
    mvc.perform(auth(patch(path).contentType("application/json")
      .content("{\"status\":\"feita\"}"))).andExpect(status().isNotFound());
    for (String andamento : new String[] { "", "publicada", "rascunho", "invalido" }) {
      mvc.perform(auth(post(path).contentType("application/json")
        .content("{\"status\":\"" + andamento + "\"}"))).andExpect(status().isBadRequest());
    }
    criar(path, "{}");
    mvc.perform(auth(post(path).contentType("application/json").content("{}")))
      .andExpect(status().isConflict());
    mvc.perform(auth(patch(path).contentType("application/json")
      .content("{\"status\":\"publicada\"}"))).andExpect(status().isBadRequest());
    mvc.perform(auth(patch(path).contentType("application/json").content("{}")))
      .andExpect(status().isBadRequest());
    mvc.perform(auth(delete(path))).andExpect(status().isNoContent());
    mvc.perform(get("/receitas/" + receita)).andExpect(status().isOk());
    mvc.perform(auth(post("/users/" + user.getId() + "/receitas/2147483647")
      .contentType("application/json").content("{}"))).andExpect(status().isNotFound());
  }

  @Test
  void excluirReceitaRemoveTodosOsAndamentosEPreservaUsuarios() throws Exception {
    int receita = receita(true);
    criar("/users/" + user.getId() + "/receitas/" + receita, "{}");
    mvc.perform(auth(post("/users/" + outro.getId() + "/receitas/" + receita)
      .contentType("application/json").content("{}"), outroToken)).andExpect(status().isCreated());
    mvc.perform(auth(delete("/receitas/" + receita))).andExpect(status().isNoContent());
    entityManager.flush();
    entityManager.clear();
    assertTrue(userRepository.existsById(user.getId()));
    assertTrue(userRepository.existsById(outro.getId()));
    mvc.perform(auth(get("/users/" + outro.getId() + "/receitas"), outroToken))
      .andExpect(content().json("[]"));
  }

  @Test
  void excluirUsuarioRemoveAndamentosSemExcluirReceitaOficial() throws Exception {
    int oficial = receitaService.cadastrar(new ReceitaRequest(null, true, "Oficial", null,
      null, "Misturar")).id();
    int minhaReceita = receita(true);
    criar("/users/" + user.getId() + "/receitas/" + oficial, "{\"status\":\"feita\"}");
    mvc.perform(auth(post("/users/" + outro.getId() + "/receitas/" + minhaReceita)
      .contentType("application/json").content("{}"), outroToken)).andExpect(status().isCreated());
    mvc.perform(auth(delete("/user/" + user.getId()))).andExpect(status().isNoContent());
    entityManager.flush();
    entityManager.clear();
    mvc.perform(get("/receitas/" + oficial)).andExpect(status().isOk());
    mvc.perform(auth(get("/users/" + outro.getId() + "/receitas"), outroToken))
      .andExpect(content().json("[]"));
  }

  private int produto(String nome) throws Exception {
    return criar("/produtos", "{\"nome\":\"" + nome + "\",\"unidade\":\"unidade\"}");
  }

  private int compra(Casa casa, String nome) throws Exception {
    return criar("/compras", "{\"casaId\":%d,\"nome\":\"%s\"}".formatted(casa.getId(), nome));
  }

  private int receita(boolean publica) throws Exception {
    return criar("/receitas", "{\"userId\":%d,\"publica\":%s,\"nome\":\"Receita\",\"preparo\":\"Misturar\"}"
      .formatted(user.getId(), publica));
  }

  private int criar(String path, String body) throws Exception {
    MvcResult result = mvc.perform(auth(post(path).contentType("application/json").content(body)))
      .andExpect(status().isCreated()).andReturn();
    return JsonPath.read(result.getResponse().getContentAsString(), "$.id");
  }

  private MockHttpServletRequestBuilder auth(MockHttpServletRequestBuilder request) {
    return auth(request, token);
  }

  private MockHttpServletRequestBuilder auth(MockHttpServletRequestBuilder request, String token) {
    return request.header("Authorization", "Bearer " + token);
  }
}
