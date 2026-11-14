package com.iagora.backend.modules.identidade.autenticacao;

public record MudarEmailRequest(String email, String novoEmail) {
}