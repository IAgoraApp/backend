package com.iagora.backend.modules.identidade.resetSenha;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reset_token")
public class TokenReset {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "user_id", nullable = false, unique = true)
  private Integer userId;

  @Column(name = "token_hash", nullable = false, length = 64)
  private String tokenHash;

  @Column(name = "expira_em", nullable = false)
  private LocalDateTime expiraEm;

  public TokenReset() {}

  public TokenReset(Integer userId, String tokenHash, LocalDateTime expiraEm) {
    this.userId = userId;
    this.tokenHash = tokenHash;
    this.expiraEm = expiraEm;
  }

  public Integer getId() {
    return id;
  }

  public Integer getUserId() {
    return userId;
  }

  public String getTokenHash() {
    return tokenHash;
  }

  public LocalDateTime getExpiraEm() {
    return expiraEm;
  }
}
