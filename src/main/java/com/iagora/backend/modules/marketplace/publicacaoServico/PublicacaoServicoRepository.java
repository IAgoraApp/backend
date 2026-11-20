package com.iagora.backend.modules.marketplace.publicacaoServico;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PublicacaoServicoRepository extends JpaRepository<PublicacaoServico, Integer> {
    List<PublicacaoServico> findByServico_IdOrderByIdDesc(Integer servicoId);
    List<PublicacaoServico> findByServico_Empresa_User_IdOrderByIdDesc(Integer userId);
}
