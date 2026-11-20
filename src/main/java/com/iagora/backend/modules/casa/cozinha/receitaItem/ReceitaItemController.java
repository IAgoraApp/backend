package com.iagora.backend.modules.casa.cozinha.receitaItem;

import io.swagger.v3.oas.annotations.tags.Tag;
import com.iagora.backend.security.MethodSecurityConfig.PodeLerReceita;
import com.iagora.backend.security.MethodSecurityConfig.SomenteProprietarioReceita;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Cozinha - Ingredientes", description = "Ingredientes utilizados em cada receita.")
@RestController
@RequestMapping("/receitas/{receitaId}/itens")
public class ReceitaItemController {

  private final ReceitaItemService receitaItemService;

  public ReceitaItemController(ReceitaItemService receitaItemService) {
    this.receitaItemService = receitaItemService;
  }

  @SomenteProprietarioReceita
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ReceitaItemResponse cadastrar(@PathVariable Integer receitaId, @RequestBody ReceitaItemRequest request) {
    return receitaItemService.cadastrar(receitaId, request);
  }

  @PodeLerReceita
  @GetMapping
  public List<ReceitaItemResponse> listar(@PathVariable Integer receitaId) {
    return receitaItemService.listar(receitaId);
  }

  @PodeLerReceita
  @GetMapping("/{id}")
  public ReceitaItemResponse buscarPorId(@PathVariable Integer receitaId, @PathVariable Integer id) {
    return receitaItemService.buscarPorId(receitaId, id);
  }

  @SomenteProprietarioReceita
  @PatchMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void atualizar(@PathVariable Integer receitaId, @PathVariable Integer id, @RequestBody ReceitaItemRequest request) {
    receitaItemService.atualizar(receitaId, id, request);
  }

  @SomenteProprietarioReceita
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void apagar(@PathVariable Integer receitaId, @PathVariable Integer id) {
    receitaItemService.apagar(receitaId, id);
  }
}
