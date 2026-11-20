package com.iagora.backend.modules.casa.cozinha.receitaFavorita;

import com.iagora.backend.exception.RegraNegocioException;
import com.iagora.backend.modules.casa.cozinha.receita.Receita;
import com.iagora.backend.modules.casa.cozinha.receita.ReceitaRepository;
import com.iagora.backend.modules.casa.cozinha.receita.ReceitaResponse;
import com.iagora.backend.modules.identidade.autenticacao.UserRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class ReceitaFavoritaService {

  private final ReceitaFavoritaRepository receitaFavoritaRepository;
  private final ReceitaRepository receitaRepository;
  private final UserRepository userRepository;

  public ReceitaFavoritaService(ReceitaFavoritaRepository receitaFavoritaRepository,
    ReceitaRepository receitaRepository, UserRepository userRepository) {
    this.receitaFavoritaRepository = receitaFavoritaRepository;
    this.receitaRepository = receitaRepository;
    this.userRepository = userRepository;
  }

  @Transactional
  public ReceitaFavoritaResponse cadastrar(Integer userId, Integer receitaId) {
    Receita receita = receitaRepository.findById(receitaId).orElseThrow(() ->
      new RegraNegocioException("Receita não encontrada", HttpStatus.NOT_FOUND)
    );
    if (!receita.getPublica()) {
      throw new RegraNegocioException("Somente receitas públicas podem ser favoritas",
        HttpStatus.BAD_REQUEST);
    }
    var user = userRepository.findById(userId).orElseThrow(() ->
      new RegraNegocioException("Usuário não encontrado", HttpStatus.NOT_FOUND)
    );
    ReceitaFavorita favorita = receitaFavoritaRepository
      .findByUser_IdAndReceita_Id(userId, receitaId).orElseGet(ReceitaFavorita::new);
    favorita.setUser(user);
    favorita.setReceita(receita);
    favorita = receitaFavoritaRepository.save(favorita);
    return new ReceitaFavoritaResponse(favorita.getId(), userId, receitaId);
  }

  @Transactional
  public List<ReceitaResponse> listar(Integer userId) {
    if (!userRepository.existsById(userId)) {
      throw new RegraNegocioException("Usuário não encontrado", HttpStatus.NOT_FOUND);
    }
    return receitaFavoritaRepository.findByUser_Id(userId).stream()
      .map(ReceitaFavorita::getReceita)
      .filter(receita -> receita.getPublica())
      .map(receita -> new ReceitaResponse(receita.getId(),
        receita.getUser() == null ? null : receita.getUser().getId(),
        receita.getPublica(), receita.getNome(), receita.getDescricao(),
        receita.getTipo(), receita.getPreparo(), receita.getImagem(), "publicada"))
      .toList();
  }

  public FavoritoStatusResponse status(Integer userId, Integer receitaId) {
    return new FavoritoStatusResponse(
      receitaFavoritaRepository.existsByUser_IdAndReceita_Id(userId, receitaId)
    );
  }

  @Transactional
  public void apagar(Integer userId, Integer receitaId) {
    receitaFavoritaRepository.deleteByUser_IdAndReceita_Id(userId, receitaId);
  }
}
