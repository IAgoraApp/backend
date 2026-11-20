package com.iagora.backend.modules.casa.cozinha.receitaUsuario;

import io.swagger.v3.oas.annotations.tags.Tag;
import com.iagora.backend.security.MethodSecurityConfig.SomenteProprietario;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Cozinha - Andamento das receitas", description = "Andamento pessoal das receitas: fazer, fazendo ou feita.")
@RestController
@RequestMapping("/users/{userId}/receitas")
public class ReceitaUsuarioController {

  private final ReceitaUsuarioService receitaUsuarioService;

  public ReceitaUsuarioController(ReceitaUsuarioService receitaUsuarioService) {
    this.receitaUsuarioService = receitaUsuarioService;
  }

  @SomenteProprietario
  @PostMapping("/{receitaId}")
  @ResponseStatus(HttpStatus.CREATED)
  public ReceitaUsuarioResponse cadastrar(@PathVariable Integer userId,
    @PathVariable Integer receitaId, @RequestBody ReceitaUsuarioRequest request) {
    return receitaUsuarioService.cadastrar(userId, receitaId, request);
  }

  @SomenteProprietario
  @GetMapping
  public List<ReceitaUsuarioResponse> listar(@PathVariable Integer userId) {
    return receitaUsuarioService.listar(userId);
  }

  @SomenteProprietario
  @GetMapping("/{receitaId}")
  public ReceitaUsuarioResponse buscarPorReceitaId(@PathVariable Integer userId,
    @PathVariable Integer receitaId) {
    return receitaUsuarioService.buscarPorReceitaId(userId, receitaId);
  }

  @SomenteProprietario
  @PatchMapping("/{receitaId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void atualizar(@PathVariable Integer userId, @PathVariable Integer receitaId,
    @RequestBody ReceitaUsuarioRequest request) {
    receitaUsuarioService.atualizar(userId, receitaId, request);
  }

  @SomenteProprietario
  @DeleteMapping("/{receitaId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void apagar(@PathVariable Integer userId, @PathVariable Integer receitaId) {
    receitaUsuarioService.apagar(userId, receitaId);
  }
}
