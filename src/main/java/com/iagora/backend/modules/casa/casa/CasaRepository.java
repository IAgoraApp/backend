package com.iagora.backend.modules.casa.casa;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CasaRepository extends JpaRepository<Casa, Integer> {

    List<Casa> findByUser_Id(Integer userId);

    boolean existsByUser_Id(Integer userId);

    void deleteByUser_Id(Integer userId);
}
