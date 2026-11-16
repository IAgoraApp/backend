package com.iagora.backend.modules.identidade.perfil;

import com.iagora.backend.security.MethodSecurityConfig.SomenteProprietario;
import com.iagora.backend.security.MethodSecurityConfig.SomenteProprietarioPerfil;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/perfis")
public class PerfilController {

  private final PerfilService perfilService;

  public PerfilController(PerfilService perfilService) {
    this.perfilService = perfilService;
  }

  @SomenteProprietarioPerfil
  @GetMapping("/{id}")
  public PerfilResponse buscarPorId(@PathVariable Integer id) {
    return perfilService.buscarPorId(id);
  }

  @SomenteProprietario
  @GetMapping("/user/{userId}")
  public PerfilResponse buscarPorUserId(@PathVariable Integer userId) {
    return perfilService.buscarPorUserId(userId);
  }

  @SomenteProprietarioPerfil
  @PatchMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void atualizar(
    @PathVariable Integer id,
    @RequestBody PerfilRequest request
  ) {
    perfilService.atualizar(id, request);
  }
}
