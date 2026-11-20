package com.iagora.backend.modules.casa.cozinha.compras;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ComprasRepository extends JpaRepository<Compras, Integer> {

    List<Compras> findByCasa_Id(Integer casaId);

    void deleteByCasa_User_Id(Integer userId);
}
