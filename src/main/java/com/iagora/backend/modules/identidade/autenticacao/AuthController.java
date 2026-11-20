package com.iagora.backend.modules.identidade.autenticacao;

import io.swagger.v3.oas.annotations.tags.Tag;
import com.iagora.backend.security.MethodSecurityConfig.SomenteProprietario;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Identidade - Autenticação", description = "Cadastro, login e gerenciamento da conta.")
@RestController
public class AuthController {

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/user/register")
  @ResponseStatus(HttpStatus.CREATED)
  public AuthResponse cadastrar(@RequestBody CadastroRequest request) {
    return authService.cadastrar(request);
  }

  @PostMapping("/user/login")
  @ResponseStatus(HttpStatus.OK)
  public LoginResponse logar(@RequestBody LoginRequest request) {
    return authService.logar(request);
  }

  @SomenteProprietario
  @GetMapping({"/user/{id}", "/users/{id}"})
  public AuthResponse buscarPorId(@PathVariable Integer id) {
    return authService.buscarPorId(id);
  }

  @SomenteProprietario
  @DeleteMapping({"/user/{id}", "/users/{id}"})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void apagarUsuario(@PathVariable Integer id) {
    authService.apagarUsuario(id);
  }

  @SomenteProprietario
  @PatchMapping({"/user/{id}", "/users/{id}"})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void atualizarEmail(
    @PathVariable Integer id,
    @RequestBody MudarEmailRequest request
  ) {
    authService.mudarEmail(id, request);
  }

  @PostMapping("/user/reset-password")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void redefinirSenha(@RequestBody MudarSenhaRequest request) {
    authService.redefinirSenha(request);
  }

  @PostMapping("/user/forgot-password")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void esqueciSenha(@RequestBody TokenResetRequest request) {
    authService.esqueciSenha(request);
  }
}
