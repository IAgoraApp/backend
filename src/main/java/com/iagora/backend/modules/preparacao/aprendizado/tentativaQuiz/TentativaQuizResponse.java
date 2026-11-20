package com.iagora.backend.modules.preparacao.aprendizado.tentativaQuiz;

import java.math.BigDecimal;

public record TentativaQuizResponse(Integer id, Integer userId, Integer quizId, BigDecimal pontuacao) {}
