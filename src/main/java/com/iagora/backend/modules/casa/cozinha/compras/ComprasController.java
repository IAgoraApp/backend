package com.iagora.backend.modules.casa.cozinha.compras;

import io.swagger.v3.oas.annotations.tags.Tag;
import com.iagora.backend.security.MethodSecurityConfig.SomenteProprietarioCasa;
import com.iagora.backend.security.MethodSecurityConfig.SomenteProprietarioCasaRequest;
import com.iagora.backend.security.MethodSecurityConfig.SomenteProprietarioCompras;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Cozinha - Listas de compras", description = "Gerenciamento das listas de compras de cada casa.")
@RestController
@RequestMapping("/compras")
public class ComprasController {

  private final ComprasService comprasService;

  public ComprasController(ComprasService comprasService) {
    this.comprasService = comprasService;
  }

  @SomenteProprietarioCasaRequest
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ComprasResponse cadastrar(@RequestBody ComprasRequest request) {
    return comprasService.cadastrar(request);
  }

  @SomenteProprietarioCompras
  @GetMapping("/{id}")
  public ComprasResponse buscarPorId(@PathVariable Integer id) {
    return comprasService.buscarPorId(id);
  }

  @SomenteProprietarioCasa
  @GetMapping("/casa/{casaId}")
  public List<ComprasResponse> buscarPorCasaId(@PathVariable Integer casaId) {
    return comprasService.buscarPorCasaId(casaId);
  }

  @SomenteProprietarioCompras
  @PatchMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void atualizar(@PathVariable Integer id, @RequestBody ComprasRequest request) {
    comprasService.atualizar(id, request);
  }

  @SomenteProprietarioCompras
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void apagar(@PathVariable Integer id) {
    comprasService.apagar(id);
  }
}
