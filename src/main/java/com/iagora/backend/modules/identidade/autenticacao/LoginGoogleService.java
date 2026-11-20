package com.iagora.backend.modules.identidade.autenticacao;

import com.iagora.backend.exception.RegraNegocioException;
import jakarta.transaction.Transactional;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Service;

@Service
public class LoginGoogleService {

  private static final Set<String> EMISSORES = Set.of(
    "accounts.google.com", "https://accounts.google.com"
  );

  private final UserRepository usuarios;
  private final AuthService auth;
  private final String clientId;
  private final NimbusJwtDecoder decoder;

  public LoginGoogleService(
    UserRepository usuarios,
    AuthService auth,
    @Value("${GOOGLE_CLIENT_ID:}") String clientId
  ) {
    this.usuarios = usuarios;
    this.auth = auth;
    this.clientId = clientId.trim();
    this.decoder = NimbusJwtDecoder
      .withJwkSetUri("https://www.googleapis.com/oauth2/v3/certs")
      .build();
  }

  @Transactional
  public LoginResponse logar(String credencial, String senha, Integer tipo) {
    if (clientId.isBlank()) {
      throw new RegraNegocioException(
        "O acesso com Google está indisponível.", HttpStatus.SERVICE_UNAVAILABLE
      );
    }
    if (credencial == null || credencial.isBlank() || credencial.length() > 8192) {
      throw credencialInvalida();
    }

    Jwt identidade;
    try {
      identidade = decoder.decode(credencial);
    } catch (JwtException erro) {
      throw credencialInvalida();
    }
    String emissor = identidade.getClaimAsString("iss");
    String sujeito = identidade.getSubject();
    if (emissor == null || !EMISSORES.contains(emissor) ||
        identidade.getAudience() == null || !identidade.getAudience().contains(clientId) ||
        (identidade.getAudience().size() > 1 &&
          !clientId.equals(identidade.getClaimAsString("azp"))) ||
        identidade.getExpiresAt() == null || sujeito == null || sujeito.isBlank()) {
      throw credencialInvalida();
    }

    var vinculado = usuarios.findByGoogleSubject(sujeito);
    if (vinculado.isPresent()) return auth.iniciarSessao(vinculado.get());

    String email = identidade.getClaimAsString("email");
    if (email == null || !Boolean.TRUE.equals(identidade.getClaimAsBoolean("email_verified"))) {
      throw credencialInvalida();
    }

    var existente = usuarios.findByEmailIgnoreCase(email);
    if (existente.isPresent()) {
      return auth.iniciarSessao(vincularContaExistente(existente.get(), sujeito, senha));
    }

    var cadastro = auth.cadastrar(new CadastroRequest(email, UUID.randomUUID().toString(), tipo == null ? 1 : tipo));
    User criado = usuarios.findById(cadastro.id()).orElseThrow();
    criado.setGoogleSubject(sujeito);
    var sessao = auth.iniciarSessao(usuarios.save(criado));
    return new LoginResponse(sessao.id(), sessao.email(), sessao.tipo(), sessao.token(), true);
  }

  private User vincularContaExistente(User usuario, String sujeito, String senha) {
    if (usuario.getGoogleSubject() != null &&
        !usuario.getGoogleSubject().equals(sujeito)) throw credencialInvalida();
    if (usuario.getGoogleSubject() != null) return usuario;
    if (senha == null || senha.isBlank()) {
      throw new RegraNegocioException(
        "Confirme a senha da sua conta IAgora para vincular o Google.",
        HttpStatus.CONFLICT
      );
    }
    if (!auth.senhaCorreta(usuario, senha)) {
      throw new RegraNegocioException(
        "Senha inválida.", HttpStatus.UNAUTHORIZED
      );
    }
    usuario.setGoogleSubject(sujeito);
    return usuarios.save(usuario);
  }

  private static RegraNegocioException credencialInvalida() {
    return new RegraNegocioException("Credencial Google inválida.", HttpStatus.UNAUTHORIZED);
  }
}
