package com.iagora.backend.modules.identidade.autenticacao;

import com.iagora.backend.exception.RegraNegocioException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

  private final UserRepository userRepository;
  private final BCryptPasswordEncoder passwordEncoder =
    new BCryptPasswordEncoder();

  public AuthService(UserRepository userRepository) {
    this.userRepository = userRepository;
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

  public AuthResponse logar(LoginRequest request) {
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

    return new AuthResponse(user.getId(), user.getEmail(), user.getTipo());
  }
}
