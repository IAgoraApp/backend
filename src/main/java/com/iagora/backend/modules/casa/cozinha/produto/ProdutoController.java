package com.iagora.backend.modules.casa.cozinha.produto;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Cozinha - Produtos", description = "Catálogo compartilhado de produtos.")
@RestController
@RequestMapping("/produtos")
public class ProdutoController {

  private final ProdutoService produtoService;

  public ProdutoController(ProdutoService produtoService) {
    this.produtoService = produtoService;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ProdutoResponse cadastrar(@RequestBody ProdutoRequest request) {
    return produtoService.cadastrar(request);
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
  public void atualizar(@PathVariable Integer id, @RequestBody ProdutoRequest request) {
    produtoService.atualizar(id, request);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void apagar(@PathVariable Integer id) {
    produtoService.apagar(id);
  }
}
