package com.iagora.backend.modules.identidade.resetSenha;

import com.iagora.backend.exception.RegraNegocioException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TokenResetService {

  private final TokenResetRepository tokenResetRepository;

  public TokenResetService(TokenResetRepository tokenResetRepository) {
    this.tokenResetRepository = tokenResetRepository;
  }

  private String gerarToken() {
    byte[] bytes = new byte[32];

    SecureRandom secureRandom = new SecureRandom();
    secureRandom.nextBytes(bytes);

    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

  private String gerarHash(String token) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");

      byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(hash);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("Erro ao gerar Hash", e);
    }
  }

  @Transactional
  public String criarToken(Integer userId) {
    tokenResetRepository
      .findByUserId(userId)
      .ifPresent(tokenResetRepository::delete);

    String token = gerarToken();
    String tokenHash = gerarHash(token);

    LocalDateTime expiracao = LocalDateTime.now().plusMinutes(15);

    TokenReset tokenReset = new TokenReset(userId, tokenHash, expiracao);

    tokenResetRepository.save(tokenReset);

    return token;
  }

  public TokenReset validarToken(String token) {
    String tokenHash = gerarHash(token);

    TokenReset tokenReset = tokenResetRepository
      .findByTokenHash(tokenHash)
      .orElseThrow(() ->
        new RegraNegocioException(
          "Token inválido ou expirado",
          HttpStatus.BAD_REQUEST
        )
      );

    if (!tokenReset.getExpiraEm().isAfter(LocalDateTime.now())) {
      throw new RegraNegocioException(
        "Token inválido ou expirado",
        HttpStatus.BAD_REQUEST
      );
    }

    return tokenReset;
  }

  public void apagarToken(TokenReset tokenReset){
    tokenResetRepository.delete(tokenReset);
  }
}
