package com.iagora.backend.modules.casa.cozinha.estoqueItem;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface EstoqueItemRepository extends JpaRepository<EstoqueItem, Integer> {

    List<EstoqueItem> findByCasa_Id(Integer casaId);

    Optional<EstoqueItem> findByCasa_IdAndProduto_Id(Integer casaId, Integer produtoId);

    boolean existsByProduto_Id(Integer produtoId);

    void deleteByCasa_User_Id(Integer userId);
}
