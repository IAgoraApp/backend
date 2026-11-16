package com.iagora.backend.modules.casa.limpeza.comodo;

import com.iagora.backend.security.MethodSecurityConfig.SomenteProprietarioCasa;
import com.iagora.backend.security.MethodSecurityConfig.SomenteProprietarioCasaRequest;
import com.iagora.backend.security.MethodSecurityConfig.SomenteProprietarioComodo;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Limpeza - Cômodos", description = "Gerenciamento dos cômodos de cada casa.")
@RestController
@RequestMapping("/comodos")
public class ComodoController {

  private final ComodoService comodoService;

  public ComodoController(ComodoService comodoService) {
    this.comodoService = comodoService;
  }

  @SomenteProprietarioCasaRequest
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ComodoResponse cadastrar(@RequestBody ComodoRequest request) {
    return comodoService.cadastrar(request);
  }

  @SomenteProprietarioComodo
  @GetMapping("/{id}")
  public ComodoResponse buscarPorId(@PathVariable Integer id) {
    return comodoService.buscarPorId(id);
  }

  @SomenteProprietarioCasa
  @GetMapping("/casa/{casaId}")
  public List<ComodoResponse> buscarPorCasaId(@PathVariable Integer casaId) {
    return comodoService.buscarPorCasaId(casaId);
  }

  @SomenteProprietarioComodo
  @PatchMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void atualizar(@PathVariable Integer id, @RequestBody ComodoRequest request) {
    comodoService.atualizar(id, request);
  }

  @SomenteProprietarioComodo
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void apagar(@PathVariable Integer id) {
    comodoService.apagar(id);
  }
}
