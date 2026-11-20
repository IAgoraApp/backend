package com.iagora.backend.modules.preparacao.jornada.jornada;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JornadaRepository extends JpaRepository<Jornada, Integer> {
    List<Jornada> findByUser_IdOrderByIdAsc(Integer userId);
}
