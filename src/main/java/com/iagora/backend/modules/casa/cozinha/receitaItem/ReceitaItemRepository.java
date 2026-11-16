package com.iagora.backend.modules.casa.cozinha.receitaItem;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ReceitaItemRepository extends JpaRepository<ReceitaItem, Integer> {

    List<ReceitaItem> findByReceita_Id(Integer receitaId);

    Optional<ReceitaItem> findByIdAndReceita_Id(Integer id, Integer receitaId);

    boolean existsByProduto_Id(Integer produtoId);

    void deleteByReceita_Id(Integer receitaId);

    void deleteByReceita_User_Id(Integer userId);
}
