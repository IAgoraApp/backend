package com.iagora.backend.modules.casa.cozinha.receitaUsuario;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReceitaUsuarioRepository extends JpaRepository<ReceitaUsuario, Integer> {

    List<ReceitaUsuario> findByUser_Id(Integer userId);

    Optional<ReceitaUsuario> findByUser_IdAndReceita_Id(Integer userId, Integer receitaId);

    boolean existsByUser_IdAndReceita_Id(Integer userId, Integer receitaId);

    void deleteByReceita_Id(Integer receitaId);

    void deleteByUser_IdOrReceita_User_Id(Integer userId, Integer receitaUserId);
}
