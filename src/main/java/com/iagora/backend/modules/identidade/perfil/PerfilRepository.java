package com.iagora.backend.modules.identidade.perfil;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PerfilRepository extends JpaRepository<Perfil, Integer> {

    Optional<Perfil> findByUser_Id(Integer userId);

    boolean existsByUser_Id(Integer userId);

    void deleteByUser_Id(Integer userId);
}
