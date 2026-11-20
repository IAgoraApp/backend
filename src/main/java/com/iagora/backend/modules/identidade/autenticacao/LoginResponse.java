package com.iagora.backend.modules.identidade.autenticacao;

public record LoginResponse(Integer id, String email, Integer tipo, String token, boolean novaConta) {
  public LoginResponse(Integer id, String email, Integer tipo, String token) {
    this(id, email, tipo, token, false);
  }
}
