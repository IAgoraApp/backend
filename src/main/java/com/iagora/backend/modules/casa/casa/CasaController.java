package com.iagora.backend.modules.casa.casa;

import io.swagger.v3.oas.annotations.tags.Tag;
import com.iagora.backend.security.MethodSecurityConfig.SomenteProprietario;
import com.iagora.backend.security.MethodSecurityConfig.SomenteProprietarioCasa;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Casas", description = "Consulta e atualização das casas dos usuários.")
@RestController
@RequestMapping("/casas")
public class CasaController {

  private final CasaService casaService;

  public CasaController(CasaService casaService) {
    this.casaService = casaService;
  }

  @SomenteProprietarioCasa
  @GetMapping("/{id}")
  public CasaResponse buscarPorId(@PathVariable Integer id) {
    return casaService.buscarPorId(id);
  }

  @SomenteProprietario
  @GetMapping({"/user/{userId}", "/usuario/{userId}"})
  public List<CasaResponse> buscarPorUserId(@PathVariable Integer userId) {
    return casaService.buscarPorUserId(userId);
  }

  @SomenteProprietarioCasa
  @PatchMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void atualizar(
    @PathVariable Integer id,
    @RequestBody CasaRequest request
  ) {
    casaService.atualizar(id, request);
  }
}
