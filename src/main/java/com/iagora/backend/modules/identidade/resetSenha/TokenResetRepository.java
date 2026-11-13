package com.iagora.backend.modules.identidade.resetSenha;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TokenResetRepository
  extends JpaRepository<TokenReset, Integer>
{
  Optional<TokenReset> findByUserId(Integer userId);
  Optional<TokenReset> findByTokenHash(String tokenHash);
}
  