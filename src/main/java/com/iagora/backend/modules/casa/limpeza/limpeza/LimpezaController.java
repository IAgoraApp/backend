package com.iagora.backend.modules.casa.limpeza.limpeza;

import com.iagora.backend.security.MethodSecurityConfig.SomenteProprietarioCasa;
import com.iagora.backend.security.MethodSecurityConfig.SomenteProprietarioComodo;
import com.iagora.backend.security.MethodSecurityConfig.SomenteProprietarioComodoRequest;
import com.iagora.backend.security.MethodSecurityConfig.SomenteProprietarioLimpeza;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Limpeza - Atividades", description = "Atividades de limpeza e próximas datas por cômodo.")
@RestController
@RequestMapping("/limpezas")
public class LimpezaController {

  private final LimpezaService limpezaService;

  public LimpezaController(LimpezaService limpezaService) {
    this.limpezaService = limpezaService;
  }

  @SomenteProprietarioComodoRequest
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public LimpezaResponse cadastrar(@RequestBody LimpezaRequest request) {
    return limpezaService.cadastrar(request);
  }

  @SomenteProprietarioLimpeza
  @GetMapping("/{id}")
  public LimpezaResponse buscarPorId(@PathVariable Integer id) {
    return limpezaService.buscarPorId(id);
  }

  @SomenteProprietarioComodo
  @GetMapping("/comodo/{comodoId}")
  public List<LimpezaResponse> buscarPorComodoId(@PathVariable Integer comodoId) {
    return limpezaService.buscarPorComodoId(comodoId);
  }

  @SomenteProprietarioCasa
  @GetMapping("/casa/{casaId}")
  public List<LimpezaResponse> buscarPorCasaId(@PathVariable Integer casaId) {
    return limpezaService.buscarPorCasaId(casaId);
  }

  @SomenteProprietarioLimpeza
  @PatchMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void atualizar(@PathVariable Integer id, @RequestBody LimpezaRequest request) {
    limpezaService.atualizar(id, request);
  }

  @SomenteProprietarioLimpeza
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void apagar(@PathVariable Integer id) {
    limpezaService.apagar(id);
  }
}
