package com.iagora.backend.modules.preparacao.moradia.casaDesejada;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CasaDesejadaRepository extends JpaRepository<CasaDesejada, Integer> {
    List<CasaDesejada> findByUser_IdOrderByIdAsc(Integer userId);
}
