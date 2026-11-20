package com.iagora.backend.modules.assistente;

import com.iagora.backend.exception.RegraNegocioException;
import com.iagora.backend.modules.identidade.autenticacao.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/assistente/historico")
public class HistoricoAssistenteController {

  private final ConversaAssistenteRepository conversas;
  private final EntityManager entidades;
  private final ObjectMapper json;

  HistoricoAssistenteController(ConversaAssistenteRepository conversas, EntityManager entidades, ObjectMapper json) {
    this.conversas = conversas;
    this.entidades = entidades;
    this.json = json;
  }

  public record Conversa(UUID id, String titulo, JsonNode mensagens) {}
  public record Pedido(@NotBlank @Size(max = 70) String titulo, @NotNull JsonNode mensagens) {}

  @GetMapping
  public ResponseEntity<List<Conversa>> listar(JwtAuthenticationToken autenticacao) {
    var itens = conversas.findByUsuario_IdOrderByAtualizadaDesc(usuario(autenticacao)).stream()
      .map(item -> new Conversa(item.chave(), item.titulo(), json.readTree(item.mensagens()))).toList();
    return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(itens);
  }

  @PutMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Transactional
  public void salvar(@PathVariable UUID id, @Valid @RequestBody Pedido pedido, JwtAuthenticationToken autenticacao) {
    var mensagens = pedido.mensagens();
    if (!mensagens.isArray() || mensagens.isEmpty()) {
      throw new RegraNegocioException("Informe as mensagens da conversa.", HttpStatus.BAD_REQUEST);
    }
    String conteudo = json.writeValueAsString(mensagens);
    if (conteudo.length() > 2_000_000) {
      throw new RegraNegocioException("A conversa atingiu o limite do histórico.", HttpStatus.PAYLOAD_TOO_LARGE);
    }

    var userId = usuario(autenticacao);
    var titular = entidades.find(User.class, userId, LockModeType.PESSIMISTIC_WRITE);
    if (titular == null) {
      throw new RegraNegocioException("Usuário não encontrado.", HttpStatus.NOT_FOUND);
    }
    var conversa = conversas.findByUsuario_IdAndChave(userId, id)
      .orElseGet(() -> new ConversaAssistente(titular, id));
    conversa.atualizar(pedido.titulo(), conteudo);
    conversas.save(conversa);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Transactional
  public void excluir(@PathVariable UUID id, JwtAuthenticationToken autenticacao) {
    var userId = usuario(autenticacao);
    entidades.find(User.class, userId, LockModeType.PESSIMISTIC_WRITE);
    conversas.findByUsuario_IdAndChave(userId, id).ifPresent(conversas::delete);
  }

  private static Integer usuario(JwtAuthenticationToken autenticacao) {
    if (autenticacao == null) {
      throw new RegraNegocioException("Faça login para acessar seu histórico.", HttpStatus.UNAUTHORIZED);
    }
    return Integer.valueOf(autenticacao.getName());
  }
}
