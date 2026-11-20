package com.iagora.backend.modules.preparacao.aprendizado.progressoAula;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProgressoAulaRepository extends JpaRepository<ProgressoAula, Integer> {
    Optional<ProgressoAula> findByUser_IdAndAula_Id(Integer userId, Integer aulaId);
    List<ProgressoAula> findByUser_IdOrderByIdAsc(Integer userId);
}
