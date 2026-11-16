package com.iagora.backend.modules.casa.limpeza.execucaoLimpeza;

import com.iagora.backend.security.MethodSecurityConfig.SomenteProprietarioLimpeza;
import com.iagora.backend.security.MethodSecurityConfig.SomenteProprietarioPerfil;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Limpeza - Execuções", description = "Histórico das limpezas realizadas e perfis responsáveis.")
@RestController
@RequestMapping("")
public class ExecucaoLimpezaController {

  private final ExecucaoLimpezaService execucaoLimpezaService;

  public ExecucaoLimpezaController(ExecucaoLimpezaService execucaoLimpezaService) {
    this.execucaoLimpezaService = execucaoLimpezaService;
  }

  @SomenteProprietarioLimpeza
  @PostMapping("/limpezas/{limpezaId}/execucoes")
  @ResponseStatus(HttpStatus.CREATED)
  public ExecucaoLimpezaResponse cadastrar(@PathVariable Integer limpezaId,
    @RequestBody ExecucaoLimpezaRequest request) {
    return execucaoLimpezaService.cadastrar(limpezaId, request);
  }

  @SomenteProprietarioLimpeza
  @GetMapping("/limpezas/{limpezaId}/execucoes")
  public List<ExecucaoLimpezaResponse> listar(@PathVariable Integer limpezaId) {
    return execucaoLimpezaService.listar(limpezaId);
  }

  @SomenteProprietarioLimpeza
  @GetMapping("/limpezas/{limpezaId}/execucoes/{id}")
  public ExecucaoLimpezaResponse buscarPorId(@PathVariable Integer limpezaId, @PathVariable Integer id) {
    return execucaoLimpezaService.buscarPorId(limpezaId, id);
  }

  @SomenteProprietarioPerfil
  @GetMapping("/execucoes-limpeza/perfil/{perfilId}")
  public List<ExecucaoLimpezaResponse> buscarPorPerfilId(@PathVariable Integer perfilId) {
    return execucaoLimpezaService.buscarPorPerfilId(perfilId);
  }

  @SomenteProprietarioLimpeza
  @DeleteMapping("/limpezas/{limpezaId}/execucoes/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void apagar(@PathVariable Integer limpezaId, @PathVariable Integer id) {
    execucaoLimpezaService.apagar(limpezaId, id);
  }
}
