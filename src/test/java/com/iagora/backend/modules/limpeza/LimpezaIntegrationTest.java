package com.iagora.backend.modules.limpeza;

import com.iagora.backend.modules.casa.casa.Casa;
import com.iagora.backend.modules.casa.casa.CasaRepository;
import com.iagora.backend.modules.casa.limpeza.comodo.ComodoRepository;
import com.iagora.backend.modules.casa.limpeza.limpeza.LimpezaRepository;
import com.iagora.backend.modules.casa.limpeza.execucaoLimpeza.ExecucaoLimpezaRepository;
import com.iagora.backend.modules.identidade.autenticacao.User;
import com.iagora.backend.modules.identidade.autenticacao.UserRepository;
import com.iagora.backend.modules.identidade.perfil.Perfil;
import com.iagora.backend.modules.identidade.perfil.PerfilRepository;
import com.iagora.backend.security.JwtService;
import com.jayway.jsonpath.JsonPath;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
  "spring.datasource.url=jdbc:h2:mem:limpeza;MODE=MySQL;DB_CLOSE_DELAY=-1",
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
class LimpezaIntegrationTest {

  @Autowired private MockMvc mvc;
  @Autowired private UserRepository userRepository;
  @Autowired private CasaRepository casaRepository;
  @Autowired private PerfilRepository perfilRepository;
  @Autowired private ComodoRepository comodoRepository;
  @Autowired private LimpezaRepository limpezaRepository;
  @Autowired private ExecucaoLimpezaRepository execucaoLimpezaRepository;
  @Autowired private JwtService jwtService;
  @Autowired private EntityManager entityManager;

  private User user;
  private Casa casa;
  private Casa outraCasa;
  private Perfil perfil;
  private Perfil outroPerfil;
  private String token;
  private String outroToken;

  @BeforeEach
  void configurar() {
    user = userRepository.save(new User(UUID.randomUUID() + "@teste.com", "hash", 1));
    User outro = userRepository.save(new User(UUID.randomUUID() + "@teste.com", "hash", 1));
    casa = casaRepository.save(new Casa(user, "Minha casa", "Casa"));
    outraCasa = casaRepository.save(new Casa(outro, "Outra casa", "Casa"));
    perfil = perfilRepository.save(new Perfil(user, "Meu perfil", 2));
    outroPerfil = perfilRepository.save(new Perfil(outro, "Outro perfil", 2));
    token = jwtService.gerarToken(user.getId());
    outroToken = jwtService.gerarToken(outro.getId());
  }

  @Test
  void cadastraListaAtualizaEExcluiComodo() throws Exception {
    int comodo = comodo("Banheiro");
    mvc.perform(auth(get("/comodos/" + comodo))).andExpect(status().isOk())
      .andExpect(jsonPath("$.tipo").value("Banheiro"));
    mvc.perform(auth(get("/comodos/casa/" + casa.getId())))
      .andExpect(jsonPath("$[0].id").value(comodo));
    mvc.perform(auth(patch("/comodos/" + comodo).contentType("application/json")
      .content("{\"nome\":\"Suíte\",\"descricao\":\"Banheiro principal\"}")))
      .andExpect(status().isNoContent());
    mvc.perform(auth(get("/comodos/" + comodo))).andExpect(jsonPath("$.nome").value("Suíte"))
      .andExpect(jsonPath("$.tipo").value("Banheiro"));
    mvc.perform(auth(delete("/comodos/" + comodo))).andExpect(status().isNoContent());
    mvc.perform(auth(get("/comodos/" + comodo))).andExpect(status().isNotFound());
  }

  @Test
  void atividadesSaoListadasPorComodoECasaComAtualizacaoParcial() throws Exception {
    int cozinha = comodo("Cozinha");
    int sala = comodo("Sala");
    int limpeza = limpeza(cozinha);
    limpeza(sala);
    mvc.perform(auth(get("/limpezas/" + limpeza))).andExpect(status().isOk())
      .andExpect(jsonPath("$.ultimaVez").isEmpty());
    mvc.perform(auth(get("/limpezas/comodo/" + cozinha))).andExpect(jsonPath("$.length()").value(1));
    mvc.perform(auth(get("/limpezas/casa/" + casa.getId()))).andExpect(jsonPath("$.length()").value(2));
    mvc.perform(auth(patch("/limpezas/" + limpeza).contentType("application/json")
      .content("{\"nome\":\"Lavar o chão\",\"proximaVez\":\"2026-12-01\"}")))
      .andExpect(status().isNoContent());
    mvc.perform(auth(get("/limpezas/" + limpeza)))
      .andExpect(jsonPath("$.nome").value("Lavar o chão"))
      .andExpect(jsonPath("$.proximaVez").value("2026-12-01"))
      .andExpect(jsonPath("$.comodoId").value(cozinha));
  }

  @Test
  void execucaoAtualizaUltimaVezERecalculaAoExcluir() throws Exception {
    int limpeza = limpeza(comodo("Sala"));
    int primeira = execucao(limpeza, "2026-01-01T10:00:00");
    int recente = execucao(limpeza, "2026-03-01T10:00:00");
    int intermediaria = execucao(limpeza, "2026-02-01T10:00:00");
    ultimaVez(limpeza, "2026-03-01");
    mvc.perform(auth(get("/limpezas/" + limpeza + "/execucoes")))
      .andExpect(jsonPath("$[0].id").value(recente))
      .andExpect(jsonPath("$.length()").value(3));
    mvc.perform(auth(get("/limpezas/" + limpeza + "/execucoes/" + primeira)))
      .andExpect(status().isOk()).andExpect(jsonPath("$.perfilId").value(perfil.getId()));
    mvc.perform(auth(get("/execucoes-limpeza/perfil/" + perfil.getId())))
      .andExpect(jsonPath("$.length()").value(3));
    mvc.perform(auth(delete("/limpezas/" + limpeza + "/execucoes/" + intermediaria)))
      .andExpect(status().isNoContent());
    ultimaVez(limpeza, "2026-03-01");
    mvc.perform(auth(delete("/limpezas/" + limpeza + "/execucoes/" + recente)))
      .andExpect(status().isNoContent());
    ultimaVez(limpeza, "2026-01-01");
    mvc.perform(auth(delete("/limpezas/" + limpeza + "/execucoes/" + primeira)))
      .andExpect(status().isNoContent());
    mvc.perform(auth(get("/limpezas/" + limpeza))).andExpect(jsonPath("$.ultimaVez").isEmpty());
  }

  @Test
  void execucaoSemDataUsaHorarioAtualEPreservaProximaVez() throws Exception {
    int limpeza = limpeza(comodo("Quarto"));
    mvc.perform(auth(patch("/limpezas/" + limpeza).contentType("application/json")
      .content("{\"proximaVez\":\"2026-12-01\"}"))).andExpect(status().isNoContent());
    criar("/limpezas/" + limpeza + "/execucoes", "{\"perfilId\":" + perfil.getId() + "}");
    ultimaVez(limpeza, LocalDate.now().toString());
    mvc.perform(auth(get("/limpezas/" + limpeza)))
      .andExpect(jsonPath("$.proximaVez").value("2026-12-01"));
  }

  @Test
  void excluiLimpezaComHistoricoSemAfetarOutraAtividade() throws Exception {
    int comodo = comodo("Cozinha");
    int primeira = limpeza(comodo);
    int segunda = limpeza(comodo);
    execucao(primeira, "2026-01-01T10:00:00");
    execucao(segunda, "2026-01-02T10:00:00");
    mvc.perform(auth(delete("/limpezas/" + primeira))).andExpect(status().isNoContent());
    entityManager.flush();
    entityManager.clear();
    assertFalse(limpezaRepository.existsById(primeira));
    assertTrue(comodoRepository.existsById(comodo));
    assertTrue(execucaoLimpezaRepository.findByLimpeza_IdOrderByRealizadaEmDescIdDesc(primeira).isEmpty());
    mvc.perform(auth(get("/limpezas/" + segunda + "/execucoes")))
      .andExpect(jsonPath("$.length()").value(1));
  }

  @Test
  void excluiComodoComAtividadesEHistoricos() throws Exception {
    int comodo = comodo("Lavanderia");
    int limpeza = limpeza(comodo);
    execucao(limpeza, "2026-01-01T10:00:00");
    mvc.perform(auth(delete("/comodos/" + comodo))).andExpect(status().isNoContent());
    entityManager.flush();
    entityManager.clear();
    assertFalse(comodoRepository.existsById(comodo));
    assertFalse(limpezaRepository.existsById(limpeza));
    assertTrue(execucaoLimpezaRepository.findByLimpeza_IdOrderByRealizadaEmDescIdDesc(limpeza).isEmpty());
    assertTrue(casaRepository.existsById(casa.getId()));
    assertTrue(perfilRepository.existsById(perfil.getId()));
  }

  @Test
  void exclusaoDoUsuarioRemoveLimpezaAntesDoPerfilEDaCasa() throws Exception {
    int comodo = comodo("Sala");
    int limpeza = limpeza(comodo);
    int execucao = execucao(limpeza, "2026-01-01T10:00:00");
    mvc.perform(auth(delete("/user/" + user.getId()))).andExpect(status().isNoContent());
    entityManager.flush();
    entityManager.clear();
    assertFalse(userRepository.existsById(user.getId()));
    assertFalse(casaRepository.existsById(casa.getId()));
    assertFalse(perfilRepository.existsById(perfil.getId()));
    assertFalse(comodoRepository.existsById(comodo));
    assertFalse(limpezaRepository.existsById(limpeza));
    assertFalse(execucaoLimpezaRepository.existsById(execucao));
    assertTrue(casaRepository.existsById(outraCasa.getId()));
  }

  @Test
  void protegeCasasComodosAtividadesEPerfisDeOutroUsuario() throws Exception {
    int comodo = comodo("Sala");
    int limpeza = limpeza(comodo);
    int execucao = execucao(limpeza, "2026-01-01T10:00:00");
    for (String path : new String[] { "/comodos/" + comodo, "/comodos/casa/" + casa.getId(),
      "/limpezas/" + limpeza, "/limpezas/casa/" + casa.getId(), "/limpezas/comodo/" + comodo,
      "/limpezas/" + limpeza + "/execucoes", "/limpezas/" + limpeza + "/execucoes/" + execucao,
      "/execucoes-limpeza/perfil/" + perfil.getId() }) {
      mvc.perform(auth(get(path), outroToken)).andExpect(status().isForbidden());
    }
    mvc.perform(auth(post("/comodos").contentType("application/json")
      .content("{\"casaId\":%d,\"nome\":\"Sala\",\"tipo\":\"Sala\"}".formatted(outraCasa.getId()))))
      .andExpect(status().isForbidden());
    mvc.perform(auth(post("/limpezas").contentType("application/json")
      .content("{\"comodoId\":%d,\"nome\":\"Limpar\"}".formatted(comodo)), outroToken))
      .andExpect(status().isForbidden());
    mvc.perform(auth(post("/limpezas/" + limpeza + "/execucoes").contentType("application/json")
      .content("{\"perfilId\":" + outroPerfil.getId() + "}")))
      .andExpect(status().isForbidden());
    mvc.perform(auth(delete("/limpezas/" + limpeza + "/execucoes/" + execucao), outroToken))
      .andExpect(status().isForbidden());
    mvc.perform(get("/comodos/" + comodo)).andExpect(status().isUnauthorized());
  }

  @Test
  void execucaoDeOutraLimpezaNaoPodeSerConsultadaOuExcluida() throws Exception {
    int comodo = comodo("Banheiro");
    int limpeza = limpeza(comodo);
    int outra = limpeza(comodo);
    int execucao = execucao(limpeza, "2026-01-01T10:00:00");
    mvc.perform(auth(get("/limpezas/" + outra + "/execucoes/" + execucao)))
      .andExpect(status().isNotFound());
    mvc.perform(auth(delete("/limpezas/" + outra + "/execucoes/" + execucao)))
      .andExpect(status().isNotFound());
    ultimaVez(limpeza, "2026-01-01");
  }

  @Test
  void rejeitaCamposObrigatoriosEReferenciasInexistentes() throws Exception {
    mvc.perform(auth(post("/comodos").contentType("application/json").content("{}")))
      .andExpect(status().isBadRequest());
    mvc.perform(auth(post("/comodos").contentType("application/json")
      .content("{\"casaId\":%d,\"nome\":\" \",\"tipo\":\"Sala\"}".formatted(casa.getId()))))
      .andExpect(status().isBadRequest());
    int comodo = comodo("Quarto");
    mvc.perform(auth(post("/limpezas").contentType("application/json").content("{}")))
      .andExpect(status().isBadRequest());
    mvc.perform(auth(post("/limpezas").contentType("application/json")
      .content("{\"comodoId\":%d,\"nome\":\" \"}".formatted(comodo))))
      .andExpect(status().isBadRequest());
    int limpeza = limpeza(comodo);
    mvc.perform(auth(post("/limpezas/" + limpeza + "/execucoes").contentType("application/json").content("{}")))
      .andExpect(status().isBadRequest());
    mvc.perform(auth(post("/limpezas/" + limpeza + "/execucoes").contentType("application/json")
      .content("{\"perfilId\":2147483647}"))).andExpect(status().isNotFound());
    mvc.perform(auth(get("/comodos/2147483647"))).andExpect(status().isNotFound());
    mvc.perform(auth(get("/limpezas/2147483647"))).andExpect(status().isNotFound());
  }

  private int comodo(String tipo) throws Exception {
    return criar("/comodos", "{\"casaId\":%d,\"nome\":\"%s\",\"tipo\":\"%s\"}"
      .formatted(casa.getId(), tipo, tipo));
  }

  private int limpeza(int comodo) throws Exception {
    return criar("/limpezas", "{\"comodoId\":%d,\"nome\":\"Varrer\"}".formatted(comodo));
  }

  private int execucao(int limpeza, String data) throws Exception {
    return criar("/limpezas/" + limpeza + "/execucoes",
      "{\"perfilId\":%d,\"realizadaEm\":\"%s\"}".formatted(perfil.getId(), data));
  }

  private void ultimaVez(int limpeza, String data) throws Exception {
    mvc.perform(auth(get("/limpezas/" + limpeza))).andExpect(jsonPath("$.ultimaVez").value(data));
  }

  private int criar(String path, String body) throws Exception {
    var result = mvc.perform(auth(post(path).contentType("application/json").content(body)))
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
