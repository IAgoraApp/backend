package com.iagora.backend.modules.endereco;

public record EnderecoRequest(
    String estado,
    String rua,
    String bairro,
    String pais,
    String cep,
    String cidade,
    String numero,
    String complemento
) {}
