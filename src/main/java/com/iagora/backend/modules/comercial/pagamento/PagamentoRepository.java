package com.iagora.backend.modules.comercial.pagamento;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PagamentoRepository extends JpaRepository<Pagamento, Integer> {
    List<Pagamento> findByUser_IdOrderByIdDesc(Integer userId);
    boolean existsByAssinatura_IdAndStatus(Integer assinaturaId, String status);
    boolean existsByPublicacao_IdAndStatus(Integer publicacaoId, String status);
    Optional<Pagamento> findByTransacaoExternaId(String id);
}
