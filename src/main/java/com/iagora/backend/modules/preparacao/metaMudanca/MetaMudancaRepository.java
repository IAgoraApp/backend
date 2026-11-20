package com.iagora.backend.modules.preparacao.metaMudanca;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MetaMudancaRepository extends JpaRepository<MetaMudanca, Integer> {
    List<MetaMudanca> findByUser_IdOrderByIdAsc(Integer userId);
}
