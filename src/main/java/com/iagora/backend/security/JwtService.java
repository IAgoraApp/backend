package com.iagora.backend.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.time.Instant;
import java.util.Base64;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

  @Value("${jwt.secret}")
  private String secret;

  @Value("${jwt.expiration}")
  private long expiration;

  private SecretKey getChave() {
    byte[] bytes = Base64.getDecoder().decode(secret);

    return new SecretKeySpec(bytes, "HmacSHA256");
  }

  private JwtEncoder getEncoder() {
    return new NimbusJwtEncoder(new ImmutableSecret<>(getChave()));
  }

  public String gerarToken(Integer userId) {
    Instant agora = Instant.now();

    JwtClaimsSet claims = JwtClaimsSet.builder()
      .subject(userId.toString())
      .issuedAt(agora)
      .expiresAt(agora.plusMillis(expiration))
      .build();

    JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

    return getEncoder()
      .encode(JwtEncoderParameters.from(header, claims))
      .getTokenValue();
  }

  @Bean
  public JwtDecoder jwtDecoder() {
    return NimbusJwtDecoder.withSecretKey(getChave())
      .macAlgorithm(MacAlgorithm.HS256)
      .build();
  }
}
