package com.iagora.backend.modules.marketplace.servicoContratado;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServicoContratadoRepository extends JpaRepository<ServicoContratado, Integer> {
    List<ServicoContratado> findByPerfil_User_IdOrderByIdDesc(Integer userId);
    List<ServicoContratado> findByServico_Empresa_User_IdOrderByIdDesc(Integer userId);
}
