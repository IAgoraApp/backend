package com.iagora.backend.modules.identidade.autenticacao;

public record MudarSenhaRequest(String token, String novaSenha) {
    
}
