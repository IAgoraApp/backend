package com.iagora.backend.modules.preparacao.moradia.planoMoradia;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanoMoradiaRepository extends JpaRepository<PlanoMoradia, Integer> {
    List<PlanoMoradia> findByUser_IdOrderByIdAsc(Integer userId);
    java.util.Optional<PlanoMoradia> findByCasaDesejada_Id(Integer casaDesejadaId);
}
