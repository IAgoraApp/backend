package com.iagora.backend.modules.casa.cozinha.produto;

import com.iagora.backend.exception.RegraNegocioException;
import com.iagora.backend.security.AdministracaoService;
import org.springframework.security.core.Authentication;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Cozinha - Produtos", description = "Catálogo compartilhado de produtos.")
@RestController
@RequestMapping("/produtos")
public class ProdutoController {

  private final ProdutoService produtoService;
  private final AdministracaoService administracao;

  public ProdutoController(ProdutoService produtoService, AdministracaoService administracao) {
    this.produtoService = produtoService;
    this.administracao = administracao;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ProdutoResponse cadastrar(@RequestBody ProdutoRequest request, Authentication authentication) {
    return produtoService.cadastrar(request, usuario(authentication));
  }

  @GetMapping
  public List<ProdutoResponse> listar() {
    return produtoService.listar();
  }

  @GetMapping("/{id}")
  public ProdutoResponse buscarPorId(@PathVariable Integer id) {
    return produtoService.buscarPorId(id);
  }

  @GetMapping("/busca")
  public List<ProdutoResponse> buscarPorNome(@RequestParam String nome) {
    return produtoService.buscarPorNome(nome);
  }

  @PatchMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void atualizar(@PathVariable Integer id, @RequestBody ProdutoRequest request, Authentication authentication) {
    verificarEdicao(id, authentication);
    produtoService.atualizar(id, request);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void apagar(@PathVariable Integer id, Authentication authentication) {
    verificarEdicao(id, authentication);
    produtoService.apagar(id);
  }

  private void verificarEdicao(Integer id, Authentication authentication) {
    if (!produtoService.pertenceAoUsuario(id, usuario(authentication))) administracao.verificar(authentication);
  }

  private Integer usuario(Authentication authentication) {
    if (authentication == null) {
      throw new RegraNegocioException("Faça login para continuar", HttpStatus.UNAUTHORIZED);
    }
    return Integer.valueOf(authentication.getName());
  }
}
