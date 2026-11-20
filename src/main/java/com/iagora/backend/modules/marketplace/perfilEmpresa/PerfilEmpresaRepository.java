package com.iagora.backend.modules.marketplace.perfilEmpresa;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PerfilEmpresaRepository extends JpaRepository<PerfilEmpresa, Integer> {
    Optional<PerfilEmpresa> findByUser_Id(Integer userId);
    List<PerfilEmpresa> findByValidadoTrueOrderByIdAsc();
    List<PerfilEmpresa> findByStatusValidacaoOrderByIdAsc(String status);
}
