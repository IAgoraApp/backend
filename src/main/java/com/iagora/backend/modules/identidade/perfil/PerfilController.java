package com.iagora.backend.modules.identidade.perfil;

import com.iagora.backend.modules.endereco.EnderecoRequest;
import com.iagora.backend.modules.endereco.EnderecoResponse;
import com.iagora.backend.modules.identidade.autenticacao.AuthResponse;
import com.iagora.backend.modules.identidade.autenticacao.AuthService;
import com.iagora.backend.modules.identidade.autenticacao.MudarEmailRequest;
import jakarta.transaction.Transactional;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.iagora.backend.security.MethodSecurityConfig.SomenteProprietario;
import com.iagora.backend.security.MethodSecurityConfig.SomenteProprietarioPerfil;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Identidade - Perfis", description = "Consulta e atualização dos perfis dos usuários.")
@RestController
@RequestMapping("/perfis")
public class PerfilController {

  private final PerfilService perfilService;

  private final AuthService autenticacao;

  public PerfilController(PerfilService perfilService, AuthService autenticacao) {
    this.perfilService = perfilService;
    this.autenticacao = autenticacao;
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
  @SomenteProprietarioPerfil
  @PatchMapping("/{id}/identidade")
  @Transactional
  public AuthResponse salvarIdentidade(@PathVariable Integer id, @RequestBody IdentidadePerfilRequest request) {
    var usuario = perfilService.buscarPorId(id).userId();
    perfilService.atualizar(id, new PerfilRequest(request.nome(), null, null, null));
    autenticacao.mudarEmail(usuario, new MudarEmailRequest(null, request.email()));
    return autenticacao.buscarPorId(usuario);
  }

  @SomenteProprietarioPerfil
  @GetMapping("/{id}/endereco")
  public EnderecoResponse consultarEndereco(@PathVariable Integer id) {
    return perfilService.consultarEndereco(id);
  }

  @SomenteProprietarioPerfil
  @PatchMapping("/{id}/endereco")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void editarEndereco(@PathVariable Integer id, @RequestBody EnderecoRequest endereco) {
    perfilService.salvarEndereco(id, endereco);
  }

  @SomenteProprietarioPerfil
  @DeleteMapping("/{id}/endereco")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void removerEndereco(@PathVariable Integer id) {
    perfilService.removerEndereco(id);
  }
}
