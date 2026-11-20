package com.iagora.backend.modules.casa.cozinha.receitaFavorita;

import io.swagger.v3.oas.annotations.tags.Tag;
import com.iagora.backend.modules.casa.cozinha.receita.ReceitaResponse;
import com.iagora.backend.security.MethodSecurityConfig.SomenteProprietario;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Cozinha - Favoritos", description = "Receitas favoritas de cada usuário.")
@RestController
@RequestMapping("")
public class ReceitaFavoritaController {

  private final ReceitaFavoritaService receitaFavoritaService;

  public ReceitaFavoritaController(ReceitaFavoritaService receitaFavoritaService) {
    this.receitaFavoritaService = receitaFavoritaService;
  }

  @SomenteProprietario
  @PostMapping("/receitas/{receitaId}/favoritos")
  @ResponseStatus(HttpStatus.CREATED)
  public ReceitaFavoritaResponse cadastrar(@RequestParam Integer userId, @PathVariable Integer receitaId) {
    return receitaFavoritaService.cadastrar(userId, receitaId);
  }

  @SomenteProprietario
  @GetMapping("/users/{userId}/receitas-favoritas")
  public List<ReceitaResponse> listar(@PathVariable Integer userId) {
    return receitaFavoritaService.listar(userId);
  }

  @SomenteProprietario
  @GetMapping("/receitas/{receitaId}/favoritos/status")
  public FavoritoStatusResponse status(@RequestParam Integer userId, @PathVariable Integer receitaId) {
    return receitaFavoritaService.status(userId, receitaId);
  }

  @SomenteProprietario
  @DeleteMapping("/receitas/{receitaId}/favoritos")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void apagar(@RequestParam Integer userId, @PathVariable Integer receitaId) {
    receitaFavoritaService.apagar(userId, receitaId);
  }
}
