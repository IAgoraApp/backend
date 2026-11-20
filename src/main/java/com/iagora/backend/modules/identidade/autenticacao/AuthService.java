package com.iagora.backend.modules.identidade.autenticacao;

import com.iagora.backend.exception.RegraNegocioException;
import com.iagora.backend.modules.casa.casa.CasaService;
import com.iagora.backend.modules.casa.cozinha.receita.ReceitaService;
import com.iagora.backend.modules.identidade.perfil.PerfilService;
import com.iagora.backend.modules.identidade.resetSenha.EmailService;
import com.iagora.backend.modules.identidade.resetSenha.TokenReset;
import com.iagora.backend.modules.identidade.resetSenha.TokenResetService;
import com.iagora.backend.security.JwtService;
import jakarta.transaction.Transactional;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.util.UriComponentsBuilder;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

  @Value("${IAGORA_FRONTEND_URL:https://iagoraweb.com.br}")
  private String frontendUrl;

  private final UserRepository userRepository;
  private final JwtService jwtService;
  private final BCryptPasswordEncoder passwordEncoder =
    new BCryptPasswordEncoder();
  private final TokenResetService tokenResetService;
  private final EmailService emailService;
  private final PerfilService perfilService;
  private final CasaService casaService;
  private final ReceitaService receitaService;

  public AuthService(
    UserRepository userRepository,
    JwtService jwtService,
    TokenResetService tokenResetService,
    EmailService emailService,
    PerfilService perfilService,
    CasaService casaService,
    ReceitaService receitaService
  ) {
    this.userRepository = userRepository;
    this.jwtService = jwtService;
    this.tokenResetService = tokenResetService;
    this.emailService = emailService;
    this.perfilService = perfilService;
    this.casaService = casaService;
    this.receitaService = receitaService;
  }

  @Transactional
  public AuthResponse cadastrar(CadastroRequest request) {
    String email = validarEmail(request.email());
    validarSenha(request.senha());
    if (!Integer.valueOf(1).equals(request.tipo()) && !Integer.valueOf(2).equals(request.tipo())) {
      throw new RegraNegocioException("Tipo de conta inválido", HttpStatus.BAD_REQUEST);
    }
    boolean emailExiste = userRepository.existsByEmail(email);

    if (emailExiste) {
      throw new RegraNegocioException(
        "Email já cadastrado",
        HttpStatus.CONFLICT
      );
    }

    String senhaHash = passwordEncoder.encode(request.senha());

    User usuario = new User(email, senhaHash, request.tipo());

    User user = userRepository.save(usuario);

    if (Integer.valueOf(1).equals(user.getTipo())) {
      perfilService.criar(user);
      casaService.criar(user);
    }

    return new AuthResponse(user.getId(), user.getEmail(), user.getTipo());
  }

  public LoginResponse logar(LoginRequest request) {
    User user = userRepository
      .findByEmail(validarEmail(request.email()))
      .orElseThrow(() ->
        new RegraNegocioException(
          "Email ou senha inválidos",
          HttpStatus.UNAUTHORIZED
        )
      );

    if (!senhaCorreta(user, request.senha())) {
      throw new RegraNegocioException(
        "Email ou senha inválidos",
        HttpStatus.UNAUTHORIZED
      );
    }

    return iniciarSessao(user);
  }

  boolean senhaCorreta(User usuario, String senha) {
    return senha != null && passwordEncoder.matches(senha, usuario.getSenha());
  }

  LoginResponse iniciarSessao(User user) {
    String token = jwtService.gerarToken(user.getId());

    return new LoginResponse(
      user.getId(),
      user.getEmail(),
      user.getTipo(),
      token
    );
  }

  public AuthResponse buscarPorId(Integer id) {
    User user = userRepository
      .findById(id)
      .orElseThrow(() ->
        new RegraNegocioException(
          "Usuário não encontrado",
          HttpStatus.NOT_FOUND
        )
      );

    return new AuthResponse(user.getId(), user.getEmail(), user.getTipo());
  }

  @Transactional
  public void apagarUsuario(Integer id) {
    User user = userRepository
      .findById(id)
      .orElseThrow(() ->
        new RegraNegocioException(
          "Usuário não encontrado",
          HttpStatus.NOT_FOUND
        )
      );

    receitaService.apagarPorUserId(id);
    casaService.apagarPorUserId(id);
    perfilService.apagarPorUserId(id);
    userRepository.delete(user);
  }

  @Transactional
  public void mudarEmail(Integer id, MudarEmailRequest request) {
    User user = userRepository
      .findById(id)
      .orElseThrow(() ->
        new RegraNegocioException(
          "Usuário não encontrado",
          HttpStatus.NOT_FOUND
        )
      );

    String email = validarEmail(request.novoEmail());
    if (email.equalsIgnoreCase(user.getEmail())) return;
    if (userRepository.existsByEmail(email)) {
      throw new RegraNegocioException(
        "Email já utilizado",
        HttpStatus.BAD_REQUEST
      );
    }

    user.setEmail(email);
    userRepository.save(user);
  }

  @Transactional
  public void redefinirSenha(MudarSenhaRequest request) {
    validarSenha(request.novaSenha());
    TokenReset tokenReset = tokenResetService.validarToken(request.token());

    User user = userRepository
      .findById(tokenReset.getUserId())
      .orElseThrow(() ->
        new RegraNegocioException(
          "Usuário não encontrado",
          HttpStatus.NOT_FOUND
        )
      );

    String senhaHash = passwordEncoder.encode(request.novaSenha());

    user.setSenha(senhaHash);
    userRepository.save(user);

    tokenResetService.apagarToken(tokenReset);
  }

  public void esqueciSenha(TokenResetRequest request) {
    String email = validarEmail(request.email());
    emailService.verificarConfiguracao();
    Optional<User> user = userRepository.findByEmail(email);

    if (user.isEmpty()) {
      return;
    }

    String token = tokenResetService.criarToken(user.get().getId());
    String link = UriComponentsBuilder.fromUriString(frontendUrl)
      .pathSegment("login").queryParam("token", token).build().toUriString();

    emailService.enviarEmail(
      user.get().getEmail(),
      "Recuperação de senha - IAgora",
      "Para redefinir sua senha, acesse:\n" + link
    );
  }
  private String validarEmail(String valor) {
    if (valor == null || valor.length() > 255 || !valor.strip().matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")) {
      throw new RegraNegocioException("Informe um e-mail válido", HttpStatus.BAD_REQUEST);
    }
    return valor.strip().toLowerCase(Locale.ROOT);
  }

  private void validarSenha(String senha) {
    if (senha == null || senha.length() < 8 || senha.getBytes(StandardCharsets.UTF_8).length > 72) {
      throw new RegraNegocioException("A senha deve ter pelo menos 8 caracteres e até 72 bytes", HttpStatus.BAD_REQUEST);
    }
  }
}
