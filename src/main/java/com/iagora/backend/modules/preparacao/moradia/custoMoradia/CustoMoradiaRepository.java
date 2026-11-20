package com.iagora.backend.modules.preparacao.moradia.custoMoradia;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustoMoradiaRepository extends JpaRepository<CustoMoradia, Integer> {
    List<CustoMoradia> findByPlano_IdOrderByIdAsc(Integer planoId);
    void deleteByPlano_IdAndTipoAndCategoriaIn(Integer planoId, String tipo, List<String> categorias);
}
