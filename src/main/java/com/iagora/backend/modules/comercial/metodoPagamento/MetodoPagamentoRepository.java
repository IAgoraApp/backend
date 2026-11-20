package com.iagora.backend.modules.comercial.metodoPagamento;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MetodoPagamentoRepository extends JpaRepository<MetodoPagamento, Integer> {
    List<MetodoPagamento> findByUser_IdOrderByIdDesc(Integer userId);
    List<MetodoPagamento> findByUser_IdAndPrincipalTrue(Integer userId);
}
