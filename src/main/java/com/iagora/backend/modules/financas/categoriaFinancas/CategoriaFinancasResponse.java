package com.iagora.backend.modules.financas.categoriaFinancas;

public record CategoriaFinancasResponse(
    Integer id,
    Integer userId,
    String nome,
    String cor,
    String icone
) {}
