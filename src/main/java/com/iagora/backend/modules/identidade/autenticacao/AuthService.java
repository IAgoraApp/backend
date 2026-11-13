package com.iagora.backend.modules.identidade.autenticacao;

import com.iagora.backend.exception.RegraNegocioException;
import com.iagora.backend.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

  private final UserRepository userRepository;
  private final JwtService jwtService;
  private final BCryptPasswordEncoder passwordEncoder =
    new BCryptPasswordEncoder();

  public AuthService(UserRepository userRepository, JwtService jwtService) {
    this.userRepository = userRepository;
    this.jwtService = jwtService;
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
        new RegraNegocioException("Usuário não encontrad", HttpStatus.NOT_FOUND)
      );

    userRepository.delete(user);
  }
}
