package com.iagora.backend.modules.casa.cozinha.receita;

import com.iagora.backend.exception.RegraNegocioException;
import com.iagora.backend.modules.casa.cozinha.receitaFavorita.ReceitaFavoritaRepository;
import com.iagora.backend.modules.casa.cozinha.receitaItem.ReceitaItemRepository;
import com.iagora.backend.modules.casa.cozinha.receitaUsuario.ReceitaUsuarioRepository;
import com.iagora.backend.modules.identidade.autenticacao.UserRepository;

import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class ReceitaService {

  private final ReceitaRepository receitaRepository;
  private final UserRepository userRepository;
  private final ReceitaItemRepository receitaItemRepository;
  private final ReceitaFavoritaRepository receitaFavoritaRepository;
  private final ReceitaUsuarioRepository receitaUsuarioRepository;

  public ReceitaService(ReceitaRepository receitaRepository, UserRepository userRepository,
    ReceitaItemRepository receitaItemRepository, ReceitaFavoritaRepository receitaFavoritaRepository,
    ReceitaUsuarioRepository receitaUsuarioRepository) {
    this.receitaRepository = receitaRepository;
    this.userRepository = userRepository;
    this.receitaItemRepository = receitaItemRepository;
    this.receitaFavoritaRepository = receitaFavoritaRepository;
    this.receitaUsuarioRepository = receitaUsuarioRepository;
  }

  @Transactional
  public ReceitaResponse cadastrar(ReceitaRequest request) {
    Receita receita = new Receita();
    if (request.userId() != null) {
      receita.setUser(userRepository.findById(request.userId()).orElseThrow(() ->
        new RegraNegocioException("Usuário não encontrado", HttpStatus.NOT_FOUND)
      ));
    }
    aplicar(receita, request);
    return resposta(receitaRepository.save(receita));
  }

  @Transactional
  public List<ReceitaResponse> listar() {
    return receitaRepository.findByPublicaTrue().stream()
      .map(this::resposta).toList();
  }

  @Transactional
  public ReceitaResponse buscarPorId(Integer id) {
    return resposta(buscar(id));
  }

  @Transactional
  public List<ReceitaResponse> buscarPorUserId(Integer userId) {
    if (!userRepository.existsById(userId)) {
      throw new RegraNegocioException("Usuário não encontrado", HttpStatus.NOT_FOUND);
    }
    return receitaRepository.findByUser_Id(userId).stream().map(this::resposta).toList();
  }

  @Transactional
  public boolean podeLer(Integer id, String userId) {
    Receita receita = buscar(id);
    return receita.getPublica() ||
      (receita.getUser() != null && receita.getUser().getId().toString().equals(userId));
  }

  @Transactional
  public boolean proprietario(Integer id, String userId) {
    Receita receita = buscar(id);
    return receita.getUser() != null && receita.getUser().getId().toString().equals(userId);
  }

  @Transactional
  public void atualizar(Integer id, ReceitaRequest request) {
    Receita receita = buscar(id);
    aplicar(receita, request);
    receitaRepository.save(receita);
  }

  @Transactional
  public void apagar(Integer id) {
    Receita receita = buscar(id);
    receitaUsuarioRepository.deleteByReceita_Id(id);
    receitaFavoritaRepository.deleteByReceita_Id(id);
    receitaItemRepository.deleteByReceita_Id(id);
    receitaRepository.delete(receita);
  }

  @Transactional
  public void apagarPorUserId(Integer userId) {
    receitaUsuarioRepository.deleteByUser_IdOrReceita_User_Id(userId, userId);
    receitaFavoritaRepository.deleteByUser_IdOrReceita_User_Id(userId, userId);
    receitaItemRepository.deleteByReceita_User_Id(userId);
    receitaRepository.deleteByUser_Id(userId);
  }

  private Receita buscar(Integer id) {
    return receitaRepository.findById(id).orElseThrow(() ->
      new RegraNegocioException("Receita não encontrada", HttpStatus.NOT_FOUND)
    );
  }

  private void aplicar(Receita receita, ReceitaRequest request) {
    if (request.publica() != null) {
      receita.setPublica(request.publica());
    }
    if (request.nome() != null) {
      receita.setNome(request.nome());
    }
    if (request.descricao() != null) {
      receita.setDescricao(request.descricao());
    }
    if (request.tipo() != null) {
      receita.setTipo(request.tipo());
    }
    if (request.preparo() != null) {
      receita.setPreparo(request.preparo());
    }

    if (receita.getNome() == null || receita.getNome().isBlank() ||
        receita.getPreparo() == null || receita.getPreparo().isBlank()) {
      throw new RegraNegocioException("Nome e preparo são obrigatórios", HttpStatus.BAD_REQUEST);
    }
  }

  private ReceitaResponse resposta(Receita receita) {
    return new ReceitaResponse(receita.getId(),
      receita.getUser() == null ? null : receita.getUser().getId(),
      receita.getPublica(), receita.getNome(), receita.getDescricao(),
      receita.getTipo(), receita.getPreparo());
  }
}
