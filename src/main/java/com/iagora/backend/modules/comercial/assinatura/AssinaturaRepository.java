package com.iagora.backend.modules.comercial.assinatura;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssinaturaRepository extends JpaRepository<Assinatura, Integer> {
    List<Assinatura> findByUser_IdOrderByIdDesc(Integer userId);
    Optional<Assinatura> findFirstByUser_IdAndStatusInOrderByIdDesc(Integer userId, List<String> status);
}
