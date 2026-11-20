package com.iagora.backend.modules.preparacao.moradia.categoriaCustoInicial;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaCustoInicialRepository extends JpaRepository<CategoriaCustoInicial, Integer> {
    List<CategoriaCustoInicial> findByUser_IdAndPlano_IdOrderByIdAsc(Integer userId, Integer planoId);
    List<CategoriaCustoInicial> findByUser_IdAndCasaDesejada_IdOrderByIdAsc(Integer userId, Integer casaDesejadaId);
}
