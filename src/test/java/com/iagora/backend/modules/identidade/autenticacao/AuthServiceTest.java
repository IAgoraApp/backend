package com.iagora.backend.modules.identidade.autenticacao;

import com.iagora.backend.modules.casa.casa.CasaService;
import com.iagora.backend.modules.identidade.perfil.PerfilService;
import com.iagora.backend.modules.identidade.resetSenha.EmailService;
import com.iagora.backend.modules.identidade.resetSenha.TokenResetService;
import com.iagora.backend.security.JwtService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class AuthServiceTest {

  private UserRepository userRepository;
  private PerfilService perfilService;
  private CasaService casaService;
  private AuthService authService;

  @BeforeEach
  void configurar() {
    userRepository = mock(UserRepository.class);
    perfilService = mock(PerfilService.class);
    casaService = mock(CasaService.class);
    authService = new AuthService(userRepository, mock(JwtService.class),
      mock(TokenResetService.class), mock(EmailService.class), perfilService, casaService);
  }

  @Test
  void cadastrarUsuarioComumCriaPerfilECasa() {
    User user = new User("user@example.com", "hash", 1);
    when(userRepository.save(any(User.class))).thenReturn(user);

    authService.cadastrar(new CadastroRequest("user@example.com", "senha", 1));

    verify(perfilService).criar(user);
    verify(casaService).criar(user);
  }

  @Test
  void cadastrarOutroTipoNaoCriaCasa() {
    when(userRepository.save(any(User.class))).thenReturn(new User("empresa@example.com", "hash", 2));

    authService.cadastrar(new CadastroRequest("empresa@example.com", "senha", 2));

    verifyNoInteractions(perfilService, casaService);
  }

  @Test
  void apagarUsuarioRemoveCasasAntesDoUsuario() {
    User user = new User("user@example.com", "hash", 1);
    when(userRepository.findById(1)).thenReturn(Optional.of(user));

    authService.apagarUsuario(1);

    var ordem = inOrder(casaService, perfilService, userRepository);
    ordem.verify(casaService).apagarPorUserId(1);
    ordem.verify(perfilService).apagarPorUserId(1);
    ordem.verify(userRepository).delete(user);
  }
}
