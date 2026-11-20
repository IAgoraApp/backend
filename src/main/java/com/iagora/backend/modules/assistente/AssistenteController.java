package com.iagora.backend.modules.assistente;

import com.iagora.backend.exception.RegraNegocioException;
import com.iagora.backend.modules.casa.casa.CasaController;
import com.iagora.backend.modules.casa.cozinha.estoqueItem.EstoqueItemController;
import com.iagora.backend.modules.casa.cozinha.compras.ComprasController;
import com.iagora.backend.modules.casa.limpeza.limpeza.LimpezaController;
import com.iagora.backend.modules.preparacao.jornada.jornada.JornadaController;
import com.iagora.backend.modules.casa.cozinha.produto.ProdutoController;
import com.iagora.backend.modules.casa.cozinha.receita.ReceitaController;
import com.iagora.backend.modules.casa.tarefa.categoriaTarefa.CategoriaTarefaController;
import com.iagora.backend.modules.casa.tarefa.execucaoTarefa.ExecucaoTarefaController;
import com.iagora.backend.modules.casa.tarefa.execucaoTarefa.ExecucaoTarefaRequest;
import com.iagora.backend.modules.casa.tarefa.tarefa.TarefaController;
import com.iagora.backend.modules.casa.tarefa.tarefa.TarefaRequest;
import com.iagora.backend.modules.casa.tarefa.tarefa.TarefaResponse;
import com.iagora.backend.modules.financas.categoriaFinancas.CategoriaFinancasController;
import com.iagora.backend.modules.financas.categoriaFinancas.CategoriaFinancasRequest;
import com.iagora.backend.modules.identidade.autenticacao.User;
import com.iagora.backend.modules.financas.transacaoFinanceira.PagarTransacaoRequest;
import com.iagora.backend.modules.financas.transacaoFinanceira.TransacaoFinanceiraController;
import com.iagora.backend.modules.financas.transacaoFinanceira.TransacaoFinanceiraRequest;
import com.iagora.backend.modules.financas.transacaoFinanceira.TransacaoFinanceiraResponse;
import com.iagora.backend.modules.preparacao.metaMudanca.MetaMudancaController;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/assistente")
public class AssistenteController {

  private final GeminiService gemini;
  private final CategoriaFinancasController categoriasFinancas;
  private final EntityManager entityManager;
  private final ReceitaController receitas;
  private final CasaController casas;
  private final ComprasController compras;
  private final LimpezaController limpezas;
  private final JornadaController jornadas;
  private final EstoqueItemController estoque;
  private final ProdutoController produtos;
  private final TarefaController tarefas;
  private final CategoriaTarefaController categorias;
  private final ExecucaoTarefaController execucoes;
  private final TransacaoFinanceiraController financas;
  private final MetaMudancaController metas;

  public AssistenteController(GeminiService gemini, ReceitaController receitas,
      TarefaController tarefas, CategoriaTarefaController categorias,
      ExecucaoTarefaController execucoes, TransacaoFinanceiraController financas, MetaMudancaController metas,
      CasaController casas, EstoqueItemController estoque, ProdutoController produtos,
      CategoriaFinancasController categoriasFinancas, EntityManager entityManager,
      ComprasController compras, LimpezaController limpezas, JornadaController jornadas) {
    this.gemini = gemini;
    this.categoriasFinancas = categoriasFinancas;
    this.entityManager = entityManager;
    this.receitas = receitas;
    this.casas = casas;
    this.compras = compras;
    this.limpezas = limpezas;
    this.jornadas = jornadas;
    this.estoque = estoque;
    this.produtos = produtos;
    this.tarefas = tarefas;
    this.categorias = categorias;
    this.execucoes = execucoes;
    this.financas = financas;
    this.metas = metas;
  }

  public record Mensagem(
      @NotBlank @Pattern(regexp = "usuario|assistente") String papel,
      @NotBlank @Size(max = 4000) String texto) {}

  public record Pedido(
      @NotNull @Size(min = 1, max = 9) List<@NotNull @Valid Mensagem> mensagens,
      Boolean preparacao) {}

  @PostMapping("/conversas")
  public ResponseEntity<GeminiService.Resposta> conversar(@Valid @RequestBody Pedido pedido,
      JwtAuthenticationToken autenticacao) {
    usuario(autenticacao);
    if (!pedido.mensagens().getLast().papel().equals("usuario")) {
      throw new RegraNegocioException("Envie uma mensagem para o assistente.", HttpStatus.BAD_REQUEST);
    }
    gemini.verificarConfiguracao();
    return ResponseEntity.ok().cacheControl(CacheControl.noStore())
      .body(gemini.responder(pedido, consultar(autenticacao)));
  }

  public record NovaTarefa(@NotBlank @Size(max = 150) String nome,
      @Size(max = 4000) String descricao, @NotNull Integer categoriaId, @NotNull LocalDateTime prazo) {}

  @PostMapping("/tarefas")
  @ResponseStatus(HttpStatus.CREATED)
  @Transactional
  public TarefaResponse criarTarefa(@Valid @RequestBody NovaTarefa pedido, JwtAuthenticationToken autenticacao) {
    usuario(autenticacao);
    var tarefa = tarefas.cadastrar(new TarefaRequest(pedido.categoriaId(),
      pedido.nome(), pedido.descricao(), pedido.prazo()), autenticacao);
    execucoes.cadastrar(tarefa.id(), new ExecucaoTarefaRequest(pedido.prazo()), autenticacao);
    return tarefa;
  }

  public record NovoLancamento(@NotBlank @Size(max = 150) String nome,
      @NotNull @Pattern(regexp = "receita|despesa") String tipo,
      @NotNull @Positive @Digits(integer = 13, fraction = 2) BigDecimal valor,
      @NotNull LocalDate data, @NotNull Boolean pago, @Positive Integer categoriaId) {}

  @PostMapping("/lancamentos")
  @ResponseStatus(HttpStatus.CREATED)
  @Transactional
  public TransacaoFinanceiraResponse criarLancamento(@Valid @RequestBody NovoLancamento pedido,
      JwtAuthenticationToken autenticacao) {
    int usuario = usuario(autenticacao);
    Integer categoriaId = pedido.categoriaId();
    if (categoriaId == null) {
      if (entityManager.find(User.class, usuario, LockModeType.PESSIMISTIC_WRITE) == null) {
        throw new RegraNegocioException("Usuário não encontrado.", HttpStatus.NOT_FOUND);
      }
      categoriaId = categoriasFinancas.listarPorUsuario(usuario, autenticacao).stream()
        .filter(categoria -> categoria.nome().equalsIgnoreCase("Outros"))
        .findFirst().orElseGet(() -> categoriasFinancas.cadastrar(
          new CategoriaFinancasRequest("Outros", null, null, null), autenticacao)).id();
    }
    boolean pago = pedido.pago();
    var lancamento = financas.cadastrar(new TransacaoFinanceiraRequest(categoriaId, null, null,
      pedido.nome(), pedido.tipo(), pedido.valor(), pedido.data().withDayOfMonth(1),
      pago ? null : pedido.data(), null), autenticacao);
    if (!pago) return lancamento;
    financas.pagar(lancamento.id(), new PagarTransacaoRequest(pedido.data()), autenticacao);
    return financas.buscarPorId(lancamento.id(), autenticacao);
  }

  private Integer usuario(JwtAuthenticationToken autenticacao) {
    if (autenticacao == null) {
      throw new RegraNegocioException("Faça login para usar o assistente.", HttpStatus.UNAUTHORIZED);
    }
    return Integer.valueOf(autenticacao.getName());
  }

  private Object consultar(JwtAuthenticationToken autenticacao) {
    var usuario = usuario(autenticacao);
    var moradias = casas.buscarPorUserId(usuario).stream().limit(5).toList();
    return Map.of(
      "receitas", receitas.buscarPorUserId(usuario).stream().limit(30)
        .map(r -> Map.of("nome", r.nome(), "tipo", r.tipo() == null ? "" : r.tipo())).toList(),
      "despensa", moradias.stream()
        .flatMap(casa -> estoque.buscarPorCasaId(casa.id()).stream())
        .limit(30).map(item -> Map.of("produto", produtos.buscarPorId(item.produtoId()).nome(),
          "quantidadeEmEstoque", item.quantidade())).toList(),
      "compras", moradias.stream().flatMap(casa -> compras.buscarPorCasaId(casa.id()).stream())
        .limit(20).map(lista -> lista.nome()).toList(),
      "limpeza", moradias.stream().flatMap(casa -> limpezas.buscarPorCasaId(casa.id()).stream())
        .limit(20).map(l -> Map.of("nome", l.nome(), "proximaVez", l.proximaVez() == null ? "" : l.proximaVez().toString())).toList(),
      "jornadas", jornadas.listarPorUsuario(usuario, autenticacao).stream().limit(20)
        .map(j -> Map.of("nome", j.nome(), "status", j.status() == null ? "" : j.status())).toList(),
      "rotina", Map.of(
        "tarefas", tarefas.listarPorUsuario(usuario, autenticacao).stream().limit(30)
          .map(t -> Map.of("nome", t.nome(), "prazo", t.prazo() == null ? "" : t.prazo().toString())).toList(),
        "categorias", categorias.listarPorUsuario(usuario, autenticacao).stream().limit(30)
          .map(c -> Map.of("id", c.id(), "nome", c.nome())).toList()),
      "financas", Map.of("resumo", financas.resumo(usuario, autenticacao),
        "categorias", categoriasFinancas.listarPorUsuario(usuario, autenticacao).stream().limit(30)
          .map(c -> Map.of("id", c.id(), "nome", c.nome())).toList()),
      "metasMudanca", metas.listar(autenticacao).stream().limit(30)
        .map(m -> Map.of("nome", m.nome(), "alvo", m.alvo() == null ? "" : m.alvo().toString(), "data",
          m.data() == null ? "" : m.data().toString())).toList());
  }
}
