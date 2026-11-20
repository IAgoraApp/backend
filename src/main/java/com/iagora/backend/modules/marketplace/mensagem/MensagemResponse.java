package com.iagora.backend.modules.marketplace.mensagem;

import java.time.LocalDateTime;

public record MensagemResponse(
    Integer id,
    Integer chatId,
    Integer userId,
    String mensagem,
    LocalDateTime data,
    LocalDateTime lidaEm
) {}
