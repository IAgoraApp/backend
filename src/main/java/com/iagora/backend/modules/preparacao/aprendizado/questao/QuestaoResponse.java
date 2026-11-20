package com.iagora.backend.modules.preparacao.aprendizado.questao;

public record QuestaoResponse(
    Integer id,
    Integer quizId,
    String enunciado
) {}
