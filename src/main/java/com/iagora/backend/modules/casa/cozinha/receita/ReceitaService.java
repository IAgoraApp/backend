package com.iagora.backend.modules.casa.cozinha.receita;

import com.iagora.backend.exception.RegraNegocioException;
import com.iagora.backend.modules.casa.cozinha.receitaItem.ReceitaItemService;
import com.iagora.backend.modules.casa.cozinha.receitaItem.ReceitaItemRequest;
import com.iagora.backend.modules.casa.cozinha.receitaFavorita.ReceitaFavoritaRepository;
import com.iagora.backend.modules.casa.cozinha.receitaItem.ReceitaItemRepository;
import com.iagora.backend.modules.casa.cozinha.receitaUsuario.ReceitaUsuarioRepository;
import com.iagora.backend.modules.identidade.autenticacao.UserRepository;

import com.iagora.backend.shared.ImagemUpload;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class ReceitaService {

  private final EntityManager entityManager;
  private final ReceitaRepository receitaRepository;
  private final UserRepository userRepository;
  private final ReceitaItemRepository receitaItemRepository;
  private final ReceitaItemService ingredientes;
  private final ReceitaFavoritaRepository receitaFavoritaRepository;
  private final ReceitaUsuarioRepository receitaUsuarioRepository;

  public ReceitaService(ReceitaRepository receitaRepository, UserRepository userRepository,
    ReceitaItemRepository receitaItemRepository, ReceitaFavoritaRepository receitaFavoritaRepository,
    ReceitaUsuarioRepository receitaUsuarioRepository, ReceitaItemService ingredientes, EntityManager entityManager) {
    this.entityManager = entityManager;
    this.receitaRepository = receitaRepository;
    this.userRepository = userRepository;
    this.receitaItemRepository = receitaItemRepository;
    this.receitaFavoritaRepository = receitaFavoritaRepository;
    this.receitaUsuarioRepository = receitaUsuarioRepository;
    this.ingredientes = ingredientes;
  }

  @Transactional
  public ReceitaResponse cadastrar(ReceitaRequest request) {
    return cadastrar(request, null);
  }

  @Transactional
  public ReceitaResponse cadastrar(ReceitaRequest request, MultipartFile arquivo) {
    Receita receita = new Receita();
    if (request.userId() != null) {
      receita.setUser(userRepository.findById(request.userId()).orElseThrow(() ->
        new RegraNegocioException("Usuário não encontrado", HttpStatus.NOT_FOUND)
      ));
    }
    aplicar(receita, request);
    receita = receitaRepository.save(receita);
    atualizarImagem(receita, request, arquivo);
    substituirIngredientes(receita.getId(), request.ingredientes());
    return resposta(receita);
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
    atualizar(id, request, null);
  }

  @Transactional
  public void atualizar(Integer id, ReceitaRequest request, MultipartFile arquivo) {
    Receita receita = buscar(id);
    entityManager.refresh(receita, LockModeType.PESSIMISTIC_WRITE);
    aplicar(receita, request);
    atualizarImagem(receita, request, arquivo);
    substituirIngredientes(id, request.ingredientes());
  }

  @Transactional
  public ReceitaImagem buscarImagem(Integer id) {
    var imagem = entityManager.find(ReceitaImagem.class, id);
    if (imagem == null) {
      throw new RegraNegocioException("Imagem não encontrada", HttpStatus.NOT_FOUND);
    }
    return imagem;
  }

  private void atualizarImagem(Receita receita, ReceitaRequest request, MultipartFile arquivo) {
    if (arquivo == null && request.imagem() == null) return;
    var imagem = entityManager.find(ReceitaImagem.class, receita.getId());
    if (arquivo == null) {
      if (imagem != null) entityManager.remove(imagem);
      return;
    }
    byte[] conteudo = ImagemUpload.ler(arquivo);
    String tipo = ImagemUpload.identificar(conteudo);
    if (imagem == null) {
      imagem = new ReceitaImagem(receita);
      imagem.substituir(tipo, conteudo);
      entityManager.persist(imagem);
    } else {
      imagem.substituir(tipo, conteudo);
    }
    receita.setImagem("/receitas/" + receita.getId() + "/imagem");
  }

  private void substituirIngredientes(Integer receitaId, List<ReceitaItemRequest> itens) {
    if (itens == null) return;
    if (itens.size() > 100 || itens.stream().anyMatch(java.util.Objects::isNull)) {
      throw new RegraNegocioException("Informe até 100 ingredientes válidos", HttpStatus.BAD_REQUEST);
    }
    receitaItemRepository.deleteByReceita_Id(receitaId);
    receitaItemRepository.flush();
    for (var item : itens) ingredientes.cadastrar(receitaId, item);
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

    if (request.imagem() != null) {
      String imagem = request.imagem().strip();
      if (!imagem.isEmpty() && (imagem.length() > 500 || !imagem.matches("(?i)https?://[^\\s]+"))) {
        throw new RegraNegocioException("Informe um endereço HTTP ou HTTPS para a imagem", HttpStatus.BAD_REQUEST);
      }
      receita.setImagem(imagem.isEmpty() ? null : imagem);
    }
    if (request.statusPublicacao() != null) {
      if (!List.of("rascunho", "publicada").contains(request.statusPublicacao())) {
        throw new RegraNegocioException("Situação da receita inválida", HttpStatus.BAD_REQUEST);
      }
      receita.setStatusPublicacao(request.statusPublicacao());
      if ("rascunho".equals(request.statusPublicacao())) receita.setPublica(false);
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
      receita.getTipo(), receita.getPreparo(), receita.getImagem(),
      receita.getPublica() ? "publicada" : receita.getStatusPublicacao());
  }
}
