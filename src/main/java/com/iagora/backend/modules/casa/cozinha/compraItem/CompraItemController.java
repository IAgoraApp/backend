package com.iagora.backend.modules.casa.cozinha.compraItem;

import io.swagger.v3.oas.annotations.tags.Tag;
import com.iagora.backend.security.MethodSecurityConfig.SomenteProprietarioCompras;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Cozinha - Itens de compra", description = "Produtos, quantidades, valores e marcação de compras.")
@RestController
@RequestMapping("/compras/{compraId}/itens")
public class CompraItemController {

  private final CompraItemService compraItemService;

  public CompraItemController(CompraItemService compraItemService) {
    this.compraItemService = compraItemService;
  }

  @SomenteProprietarioCompras
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public CompraItemResponse cadastrar(@PathVariable Integer compraId, @RequestBody CompraItemRequest request) {
    return compraItemService.cadastrar(compraId, request);
  }

  @SomenteProprietarioCompras
  @GetMapping
  public List<CompraItemResponse> listar(@PathVariable Integer compraId) {
    return compraItemService.listar(compraId);
  }

  @SomenteProprietarioCompras
  @GetMapping("/{id}")
  public CompraItemResponse buscarPorId(@PathVariable Integer compraId, @PathVariable Integer id) {
    return compraItemService.buscarPorId(compraId, id);
  }

  @SomenteProprietarioCompras
  @PatchMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void atualizar(@PathVariable Integer compraId, @PathVariable Integer id, @RequestBody CompraItemRequest request) {
    compraItemService.atualizar(compraId, id, request);
  }

  @SomenteProprietarioCompras
  @PatchMapping("/{id}/comprado")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void marcarComprado(@PathVariable Integer compraId, @PathVariable Integer id, @RequestBody CompradoRequest request) {
    compraItemService.marcarComprado(compraId, id, request);
  }

  @SomenteProprietarioCompras
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void apagar(@PathVariable Integer compraId, @PathVariable Integer id) {
    compraItemService.apagar(compraId, id);
  }
}
