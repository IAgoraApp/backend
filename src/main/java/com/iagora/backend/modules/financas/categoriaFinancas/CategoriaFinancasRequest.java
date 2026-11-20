package com.iagora.backend.modules.financas.categoriaFinancas;

public record CategoriaFinancasRequest(
    String nome,
    String cor,
    String icone,
    Integer userId
) {}
