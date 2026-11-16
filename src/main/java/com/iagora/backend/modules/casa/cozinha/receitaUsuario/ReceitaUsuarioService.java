package com.iagora.backend.modules.casa.cozinha.receitaUsuario;

import com.iagora.backend.exception.RegraNegocioException;
import com.iagora.backend.modules.casa.cozinha.receita.ReceitaRepository;
import com.iagora.backend.modules.casa.cozinha.receita.ReceitaService;
import com.iagora.backend.modules.identidade.autenticacao.UserRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class ReceitaUsuarioService {

  private final ReceitaUsuarioRepository receitaUsuarioRepository;
  private final ReceitaRepository receitaRepository;
  private final UserRepository userRepository;
  private final ReceitaService receitaService;

  public ReceitaUsuarioService(ReceitaUsuarioRepository receitaUsuarioRepository,
    ReceitaRepository receitaRepository, UserRepository userRepository, ReceitaService receitaService) {
    this.receitaUsuarioRepository = receitaUsuarioRepository;
    this.receitaRepository = receitaRepository;
    this.userRepository = userRepository;
    this.receitaService = receitaService;
  }

  @Transactional
  public ReceitaUsuarioResponse cadastrar(Integer userId, Integer receitaId, ReceitaUsuarioRequest request) {
    validarAcesso(userId, receitaId);
    if (receitaUsuarioRepository.existsByUser_IdAndReceita_Id(userId, receitaId)) {
      throw new RegraNegocioException("Receita já adicionada pelo usuário", HttpStatus.CONFLICT);
    }

    ReceitaUsuario receitaUsuario = new ReceitaUsuario();
    receitaUsuario.setUser(userRepository.findById(userId).orElseThrow(() ->
      new RegraNegocioException("Usuário não encontrado", HttpStatus.NOT_FOUND)
    ));
    receitaUsuario.setReceita(receitaRepository.findById(receitaId).orElseThrow(() ->
      new RegraNegocioException("Receita não encontrada", HttpStatus.NOT_FOUND)
    ));
    if (request.status() != null) {
      validarStatus(request.status());
      receitaUsuario.setStatus(request.status());
    }
    return resposta(receitaUsuarioRepository.save(receitaUsuario));
  }

  @Transactional
  public List<ReceitaUsuarioResponse> listar(Integer userId) {
    if (!userRepository.existsById(userId)) {
      throw new RegraNegocioException("Usuário não encontrado", HttpStatus.NOT_FOUND);
    }
    return receitaUsuarioRepository.findByUser_Id(userId).stream()
      .filter(item -> receitaService.podeLer(item.getReceita().getId(), userId.toString()))
      .map(this::resposta).toList();
  }

  @Transactional
  public ReceitaUsuarioResponse buscarPorReceitaId(Integer userId, Integer receitaId) {
    ReceitaUsuario receitaUsuario = buscar(userId, receitaId);
    validarAcesso(userId, receitaId);
    return resposta(receitaUsuario);
  }

  @Transactional
  public void atualizar(Integer userId, Integer receitaId, ReceitaUsuarioRequest request) {
    ReceitaUsuario receitaUsuario = buscar(userId, receitaId);
    validarAcesso(userId, receitaId);
    validarStatus(request.status());
    receitaUsuario.setStatus(request.status());
    receitaUsuarioRepository.save(receitaUsuario);
  }

  @Transactional
  public void apagar(Integer userId, Integer receitaId) {
    receitaUsuarioRepository.delete(buscar(userId, receitaId));
  }

  private ReceitaUsuario buscar(Integer userId, Integer receitaId) {
    return receitaUsuarioRepository.findByUser_IdAndReceita_Id(userId, receitaId).orElseThrow(() ->
      new RegraNegocioException("Receita não adicionada pelo usuário", HttpStatus.NOT_FOUND)
    );
  }

  private void validarAcesso(Integer userId, Integer receitaId) {
    if (!receitaService.podeLer(receitaId, userId.toString())) {
      throw new RegraNegocioException("Você não tem permissão para acessar esta receita", HttpStatus.FORBIDDEN);
    }
  }

  private void validarStatus(String status) {
    if (status == null || !Set.of("fazer", "fazendo", "feita").contains(status)) {
      throw new RegraNegocioException("Status deve ser fazer, fazendo ou feita", HttpStatus.BAD_REQUEST);
    }
  }

  private ReceitaUsuarioResponse resposta(ReceitaUsuario receitaUsuario) {
    return new ReceitaUsuarioResponse(receitaUsuario.getId(), receitaUsuario.getUser().getId(),
      receitaUsuario.getReceita().getId(), receitaUsuario.getStatus());
  }
}
