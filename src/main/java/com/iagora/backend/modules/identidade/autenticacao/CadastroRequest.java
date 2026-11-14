package com.iagora.backend.modules.identidade.autenticacao;

public record CadastroRequest(String email, String senha, Integer tipo) {

}