package com.iagora.backend.modules.casa.cozinha.compraItem;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CompraItemRepository extends JpaRepository<CompraItem, Integer> {

    List<CompraItem> findByCompras_Id(Integer comprasId);

    Optional<CompraItem> findByIdAndCompras_Id(Integer id, Integer comprasId);

    boolean existsByProduto_Id(Integer produtoId);

    void deleteByCompras_Id(Integer comprasId);

    void deleteByCompras_Casa_User_Id(Integer userId);
}
