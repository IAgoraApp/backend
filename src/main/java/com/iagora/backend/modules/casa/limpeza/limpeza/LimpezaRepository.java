package com.iagora.backend.modules.casa.limpeza.limpeza;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LimpezaRepository extends JpaRepository<Limpeza, Integer> {

    List<Limpeza> findByComodo_Id(Integer comodoId);

    List<Limpeza> findByComodo_Casa_Id(Integer casaId);

    void deleteByComodo_Id(Integer comodoId);

    void deleteByComodo_Casa_User_Id(Integer userId);
}
