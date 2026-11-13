package com.iagora.backend.modules.identidade.autenticacao;

public record LoginResponse(Integer id, String email, Integer tipo, String token) {}
