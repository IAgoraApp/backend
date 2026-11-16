package com.iagora.backend.modules.casa.cozinha.receita;

import io.swagger.v3.oas.annotations.tags.Tag;
import com.iagora.backend.security.MethodSecurityConfig.PodeLerReceita;
import com.iagora.backend.security.MethodSecurityConfig.SomenteProprietario;
import com.iagora.backend.security.MethodSecurityConfig.SomenteProprietarioReceita;
import com.iagora.backend.security.MethodSecurityConfig.SomenteProprietarioReceitaRequest;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Cozinha - Receitas", description = "Cadastro, consulta e edição de receitas.")
@RestController
@RequestMapping("/receitas")
public class ReceitaController {

  private final ReceitaService receitaService;

  public ReceitaController(ReceitaService receitaService) {
    this.receitaService = receitaService;
  }

  @SomenteProprietarioReceitaRequest
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ReceitaResponse cadastrar(@RequestBody ReceitaRequest request) {
    return receitaService.cadastrar(request);
  }

  @GetMapping
  public List<ReceitaResponse> listar() {
    return receitaService.listar();
  }

  @PodeLerReceita
  @GetMapping("/{id}")
  public ReceitaResponse buscarPorId(@PathVariable Integer id) {
    return receitaService.buscarPorId(id);
  }

  @SomenteProprietario
  @GetMapping("/user/{userId}")
  public List<ReceitaResponse> buscarPorUserId(@PathVariable Integer userId) {
    return receitaService.buscarPorUserId(userId);
  }

  @SomenteProprietarioReceita
  @PatchMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void atualizar(@PathVariable Integer id, @RequestBody ReceitaRequest request) {
    receitaService.atualizar(id, request);
  }

  @SomenteProprietarioReceita
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void apagar(@PathVariable Integer id) {
    receitaService.apagar(id);
  }
}
