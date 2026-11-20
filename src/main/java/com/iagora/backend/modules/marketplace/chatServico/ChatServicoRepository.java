package com.iagora.backend.modules.marketplace.chatServico;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatServicoRepository extends JpaRepository<ChatServico, Integer> {
    List<ChatServico> findByPerfil_User_IdOrEmpresa_User_IdOrderByIdDesc(Integer clienteId, Integer empresaId);
    Optional<ChatServico> findByContratado_Id(Integer contratadoId);
}
