package com.iagora.backend.modules.financas.categoriaFinancas;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaFinancasRepository extends JpaRepository<CategoriaFinancas, Integer> {
    List<CategoriaFinancas> findByUser_IdOrderByIdAsc(Integer userId);
    boolean existsByUser_IdAndNomeIgnoreCaseAndIdNot(Integer userId, String nome, Integer id);

    Optional<CategoriaFinancas> findByUser_IdAndNomeIgnoreCase(Integer userId, String nome);
}
