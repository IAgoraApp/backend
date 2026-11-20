package com.iagora.backend.modules.identidade.autenticacao;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginGoogleController {

  private final LoginGoogleService google;

  public LoginGoogleController(LoginGoogleService google) {
    this.google = google;
  }

  @PostMapping("/user/login/google")
  public LoginResponse logar(@RequestBody CredencialGoogle request) {
    return google.logar(request.credential(), request.senha(), request.tipo());
  }

  public record CredencialGoogle(String credential, String senha, Integer tipo) {}
}
