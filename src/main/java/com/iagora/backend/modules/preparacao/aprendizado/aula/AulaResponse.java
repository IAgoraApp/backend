package com.iagora.backend.modules.preparacao.aprendizado.aula;

public record AulaResponse(
    Integer id,
    String titulo,
    String descricao,
    String conteudo,
    String videoUrl,
    String imagem,
    String categoria
) {}
