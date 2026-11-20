package com.iagora.backend.modules.marketplace.imagem;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImagemMarketplaceRepository extends JpaRepository<ImagemMarketplace, UUID> {

    interface Referencia {
        UUID getId();
        java.time.LocalDateTime getCriadaEm();
    }

    List<Referencia> findByEmpresa_IdAndServicoIsNull(Integer empresaId);
    List<Referencia> findByServico_Id(Integer servicoId);
    void deleteByIdIn(List<UUID> ids);
}
