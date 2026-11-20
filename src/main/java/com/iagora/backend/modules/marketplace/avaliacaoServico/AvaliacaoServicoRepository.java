package com.iagora.backend.modules.marketplace.avaliacaoServico;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AvaliacaoServicoRepository extends JpaRepository<AvaliacaoServico, Integer> {
    List<AvaliacaoServico> findByContratado_Servico_IdOrderByIdDesc(Integer servicoId);
    List<AvaliacaoServico> findByPerfil_User_IdOrderByIdDesc(Integer userId);
    boolean existsByContratado_Id(Integer contratadoId);
}
