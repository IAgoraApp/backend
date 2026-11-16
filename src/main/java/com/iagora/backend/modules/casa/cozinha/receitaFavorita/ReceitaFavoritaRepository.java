package com.iagora.backend.modules.casa.cozinha.receitaFavorita;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ReceitaFavoritaRepository extends JpaRepository<ReceitaFavorita, Integer> {

    List<ReceitaFavorita> findByUser_Id(Integer userId);

    Optional<ReceitaFavorita> findByUser_IdAndReceita_Id(Integer userId, Integer receitaId);

    boolean existsByUser_IdAndReceita_Id(Integer userId, Integer receitaId);

    void deleteByUser_IdAndReceita_Id(Integer userId, Integer receitaId);

    void deleteByReceita_Id(Integer receitaId);

    void deleteByUser_IdOrReceita_User_Id(Integer userId, Integer receitaUserId);
}
