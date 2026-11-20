package com.iagora.backend.modules.assistente;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface ConversaAssistenteRepository extends JpaRepository<ConversaAssistente, Long> {
  List<ConversaAssistente> findByUsuario_IdOrderByAtualizadaDesc(Integer usuario);
  Optional<ConversaAssistente> findByUsuario_IdAndChave(Integer usuario, UUID chave);
}
