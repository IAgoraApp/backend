package com.iagora.backend.modules.identidade.autenticacao;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

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
  public AuthResponse logar(@RequestBody LoginRequest request) {
    return authService.logar(request);
  }
}
