package com.iagora.backend.modules.identidade.resetSenha;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TokenResetRepository
  extends JpaRepository<TokenReset, Integer>
{
  Optional<TokenReset> findByUserId(Integer userId);
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  Optional<TokenReset> findByTokenHash(String tokenHash);
}
  