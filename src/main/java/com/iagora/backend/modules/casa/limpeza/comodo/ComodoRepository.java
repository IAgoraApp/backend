package com.iagora.backend.modules.casa.limpeza.comodo;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ComodoRepository extends JpaRepository<Comodo, Integer> {

    List<Comodo> findByCasa_Id(Integer casaId);

    void deleteByCasa_User_Id(Integer userId);
}
