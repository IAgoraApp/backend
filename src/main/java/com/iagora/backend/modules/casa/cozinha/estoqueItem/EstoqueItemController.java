package com.iagora.backend.modules.casa.cozinha.estoqueItem;

import io.swagger.v3.oas.annotations.tags.Tag;
import com.iagora.backend.security.MethodSecurityConfig.SomenteProprietarioCasa;
import com.iagora.backend.security.MethodSecurityConfig.SomenteProprietarioCasaRequest;
import com.iagora.backend.security.MethodSecurityConfig.SomenteProprietarioEstoque;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Cozinha - Estoque", description = "Produtos e quantidades armazenados em cada casa.")
@RestController
@RequestMapping("/estoque-itens")
public class EstoqueItemController {

  private final EstoqueItemService estoqueItemService;

  public EstoqueItemController(EstoqueItemService estoqueItemService) {
    this.estoqueItemService = estoqueItemService;
  }

  @SomenteProprietarioCasaRequest
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public EstoqueItemResponse cadastrar(@RequestBody EstoqueItemRequest request) {
    return estoqueItemService.cadastrar(request);
  }

  @SomenteProprietarioCasa
  @PostMapping("/casa/{casaId}/adicionar")
  public EstoqueItemResponse adicionarQuantidade(
    @PathVariable Integer casaId,
    @RequestBody EstoqueItemRequest request
  ) {
    return estoqueItemService.adicionarQuantidade(casaId, request);
  }

  @SomenteProprietarioEstoque
  @GetMapping("/{id}")
  public EstoqueItemResponse buscarPorId(@PathVariable Integer id) {
    return estoqueItemService.buscarPorId(id);
  }

  @SomenteProprietarioCasa
  @GetMapping("/casa/{casaId}")
  public List<EstoqueItemResponse> buscarPorCasaId(@PathVariable Integer casaId) {
    return estoqueItemService.buscarPorCasaId(casaId);
  }

  @SomenteProprietarioEstoque
  @PatchMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void atualizar(@PathVariable Integer id, @RequestBody EstoqueItemRequest request) {
    estoqueItemService.atualizar(id, request);
  }

  @SomenteProprietarioEstoque
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void apagar(@PathVariable Integer id) {
    estoqueItemService.apagar(id);
  }
}
