package com.iagora.backend.modules.preparacao.aprendizado.tentativaQuiz;

import java.math.BigDecimal;

public record TentativaQuizResultadoResponse(Integer tentativaId, BigDecimal pontuacao, Integer totalQuestoes, Integer acertos) {}
