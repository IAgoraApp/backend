package com.iagora.backend.modules.marketplace.servicoFavorito;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServicoFavoritoRepository extends JpaRepository<ServicoFavorito, ServicoFavoritoId> {
    List<ServicoFavorito> findByUser_IdOrderByServico_IdAsc(Integer userId);
    Optional<ServicoFavorito> findByUser_IdAndServico_Id(Integer userId, Integer servicoId);
}
