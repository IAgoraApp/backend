package com.iagora.backend.modules.identidade.autenticacao;

import com.iagora.backend.security.MethodSecurityConfig.SomenteProprietario;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class AuthController {

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/register")
  @ResponseStatus(HttpStatus.CREATED)
  public AuthResponse cadastrar(@RequestBody CadastroRequest request) {
    return authService.cadastrar(request);
  }

  @PostMapping("/login")
  @ResponseStatus(HttpStatus.OK)
  public LoginResponse logar(@RequestBody LoginRequest request) {
    return authService.logar(request);
  }

  @SomenteProprietario
  @GetMapping("/{id}")
  public AuthResponse buscarPorId(@PathVariable Integer id) {
    return authService.buscarPorId(id);
  }

  @SomenteProprietario
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void apagarUsuario(@PathVariable Integer id) {
    authService.apagarUsuario(id);
  }

  @SomenteProprietario
  @PatchMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void atualizarEmail(
    @PathVariable Integer id,
    @RequestBody MudarEmailRequest request
  ) {
    authService.mudarEmail(id, request);
  }

  @PostMapping("/reset-password")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void redefinirSenha(@RequestBody MudarSenhaRequest request) {
    authService.redefinirSenha(request);
  }

  @PostMapping ("/forgot-password")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void esqueciSenha(@RequestBody TokenResetRequest request) {
    authService.esqueciSenha(request);
  }
}
