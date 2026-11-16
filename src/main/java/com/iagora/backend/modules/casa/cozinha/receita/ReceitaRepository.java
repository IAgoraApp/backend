package com.iagora.backend.modules.casa.cozinha.receita;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReceitaRepository extends JpaRepository<Receita, Integer> {

    List<Receita> findByPublicaTrue();

    List<Receita> findByUser_Id(Integer userId);

    void deleteByUser_Id(Integer userId);
}
