package com.iagora.backend.modules.identidade.autenticacao;

import com.iagora.backend.exception.RegraNegocioException;
import com.iagora.backend.modules.identidade.resetSenha.EmailService;
import com.iagora.backend.modules.identidade.resetSenha.TokenReset;
import com.iagora.backend.modules.identidade.resetSenha.TokenResetService;
import com.iagora.backend.security.JwtService;
import jakarta.transaction.Transactional;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

  private final UserRepository userRepository;
  private final JwtService jwtService;
  private final BCryptPasswordEncoder passwordEncoder =
    new BCryptPasswordEncoder();
  private final TokenResetService tokenResetService;
  private final EmailService emailService;

  public AuthService(
    UserRepository userRepository,
    JwtService jwtService,
    TokenResetService tokenResetService,
    EmailService emailService
  ) {
    this.userRepository = userRepository;
    this.jwtService = jwtService;
    this.tokenResetService = tokenResetService;
    this.emailService = emailService;
  }

  public AuthResponse cadastrar(CadastroRequest request) {
    boolean emailExiste = userRepository.existsByEmail(request.email());

    if (emailExiste) {
      throw new RegraNegocioException(
        "Email já cadastrado",
        HttpStatus.CONFLICT
      );
    }

    String senhaHash = passwordEncoder.encode(request.senha());

    User usuario = new User(request.email(), senhaHash, request.tipo());

    User user = userRepository.save(usuario);

    return new AuthResponse(user.getId(), user.getEmail(), user.getTipo());
  }

  public LoginResponse logar(LoginRequest request) {
    User user = userRepository
      .findByEmail(request.email())
      .orElseThrow(() ->
        new RegraNegocioException(
          "Email ou senha inválidos",
          HttpStatus.UNAUTHORIZED
        )
      );

    boolean senhaCorreta = passwordEncoder.matches(
      request.senha(),
      user.getSenha()
    );

    if (!senhaCorreta) {
      throw new RegraNegocioException(
        "Email ou senha inválidos",
        HttpStatus.UNAUTHORIZED
      );
    }

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

  public void apagarUsuario(Integer id) {
    User user = userRepository
      .findById(id)
      .orElseThrow(() ->
        new RegraNegocioException(
          "Usuário não encontrado",
          HttpStatus.NOT_FOUND
        )
      );

    userRepository.delete(user);
  }

  public void mudarEmail(Integer id, MudarEmailRequest request) {
    User user = userRepository
      .findById(id)
      .orElseThrow(() ->
        new RegraNegocioException(
          "Usuário não encontrado",
          HttpStatus.NOT_FOUND
        )
      );

    if (userRepository.existsByEmail(request.novoEmail())) {
      throw new RegraNegocioException(
        "Email já utilizado",
        HttpStatus.BAD_REQUEST
      );
    }

    user.setEmail(request.novoEmail());
    userRepository.save(user);
  }

  @Transactional
  public void redefinirSenha(MudarSenhaRequest request) {
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
    Optional<User> user = userRepository.findByEmail(request.email());

    if (user.isEmpty()) {
      return;
    }

    String token = tokenResetService.criarToken(user.get().getId());
    String link = "http://frontend?token=" + token;

    emailService.enviarEmail(
      user.get().getEmail(),
      "Recuperação de senha - IAgora",
      "Para redefinir sua senha, acesse:\n" + link
    );
  }
}
