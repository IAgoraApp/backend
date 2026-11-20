package com.iagora.backend.modules.preparacao.aprendizado.aula;

public record AulaRequest(
    String titulo,
    String descricao,
    String conteudo,
    String videoUrl,
    String imagem,
    String categoria
) {}
