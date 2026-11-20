package com.iagora.backend.modules.casa.casa;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CasaRepository extends JpaRepository<Casa, Integer> {

    List<Casa> findByUser_Id(Integer userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Casa> findCasaById(Integer id);

    boolean existsByUser_Id(Integer userId);

    void deleteByUser_Id(Integer userId);
}
