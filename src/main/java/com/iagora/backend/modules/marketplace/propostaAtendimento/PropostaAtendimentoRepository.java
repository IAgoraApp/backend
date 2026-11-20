package com.iagora.backend.modules.marketplace.propostaAtendimento;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PropostaAtendimentoRepository extends JpaRepository<PropostaAtendimento, Integer> {
    List<PropostaAtendimento> findByContratado_IdOrderByIdDesc(Integer contratadoId);
    Optional<PropostaAtendimento> findByContratado_IdAndStatus(Integer contratadoId, String status);
}
