package com.iagora.backend.modules.marketplace.mensagem;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MensagemRepository extends JpaRepository<Mensagem, Integer> {
    List<Mensagem> findByChat_IdOrderByIdAsc(Integer chatId);
    List<Mensagem> findByChat_IdAndUser_IdNotAndLidaEmIsNull(Integer chatId, Integer userId);
    long countByChat_IdAndUser_IdNotAndLidaEmIsNull(Integer chatId, Integer userId);
}
