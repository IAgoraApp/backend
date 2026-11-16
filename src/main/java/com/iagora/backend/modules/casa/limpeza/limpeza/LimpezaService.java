package com.iagora.backend.modules.casa.limpeza.limpeza;

import com.iagora.backend.exception.RegraNegocioException;
import com.iagora.backend.modules.casa.casa.CasaRepository;
import com.iagora.backend.modules.casa.limpeza.comodo.ComodoRepository;
import com.iagora.backend.modules.casa.limpeza.execucaoLimpeza.ExecucaoLimpezaRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class LimpezaService {

  private final LimpezaRepository limpezaRepository;
  private final ComodoRepository comodoRepository;
  private final CasaRepository casaRepository;
  private final ExecucaoLimpezaRepository execucaoLimpezaRepository;

  public LimpezaService(LimpezaRepository limpezaRepository, ComodoRepository comodoRepository,
    CasaRepository casaRepository, ExecucaoLimpezaRepository execucaoLimpezaRepository) {
    this.limpezaRepository = limpezaRepository;
    this.comodoRepository = comodoRepository;
    this.casaRepository = casaRepository;
    this.execucaoLimpezaRepository = execucaoLimpezaRepository;
  }

  @Transactional
  public LimpezaResponse cadastrar(LimpezaRequest request) {
    if (request.comodoId() == null) {
      throw new RegraNegocioException("Cômodo é obrigatório", HttpStatus.BAD_REQUEST);
    }
    Limpeza limpeza = new Limpeza();
    limpeza.setComodo(comodoRepository.findById(request.comodoId()).orElseThrow(() ->
      new RegraNegocioException("Cômodo não encontrado", HttpStatus.NOT_FOUND)
    ));
    aplicar(limpeza, request);
    return resposta(limpezaRepository.save(limpeza));
  }

  @Transactional
  public LimpezaResponse buscarPorId(Integer id) {
    return resposta(buscar(id));
  }

  @Transactional
  public List<LimpezaResponse> buscarPorComodoId(Integer comodoId) {
    if (!comodoRepository.existsById(comodoId)) {
      throw new RegraNegocioException("Cômodo não encontrado", HttpStatus.NOT_FOUND);
    }
    return limpezaRepository.findByComodo_Id(comodoId).stream().map(this::resposta).toList();
  }

  @Transactional
  public List<LimpezaResponse> buscarPorCasaId(Integer casaId) {
    if (!casaRepository.existsById(casaId)) {
      throw new RegraNegocioException("Casa não encontrada", HttpStatus.NOT_FOUND);
    }
    return limpezaRepository.findByComodo_Casa_Id(casaId).stream().map(this::resposta).toList();
  }

  @Transactional
  public void atualizar(Integer id, LimpezaRequest request) {
    Limpeza limpeza = buscar(id);
    aplicar(limpeza, request);
    limpezaRepository.save(limpeza);
  }

  @Transactional
  public void apagar(Integer id) {
    Limpeza limpeza = buscar(id);
    execucaoLimpezaRepository.deleteByLimpeza_Id(id);
    limpezaRepository.delete(limpeza);
  }

  private Limpeza buscar(Integer id) {
    return limpezaRepository.findById(id).orElseThrow(() ->
      new RegraNegocioException("Limpeza não encontrada", HttpStatus.NOT_FOUND)
    );
  }

  private void aplicar(Limpeza limpeza, LimpezaRequest request) {
    if (request.nome() != null) {
      limpeza.setNome(request.nome());
    }
    if (request.descricao() != null) {
      limpeza.setDescricao(request.descricao());
    }
    if (request.proximaVez() != null) {
      limpeza.setProximaVez(request.proximaVez());
    }
    if (limpeza.getNome() == null || limpeza.getNome().isBlank()) {
      throw new RegraNegocioException("Nome é obrigatório", HttpStatus.BAD_REQUEST);
    }
  }

  private LimpezaResponse resposta(Limpeza limpeza) {
    return new LimpezaResponse(limpeza.getId(), limpeza.getComodo().getId(), limpeza.getNome(),
      limpeza.getDescricao(), limpeza.getUltimaVez(), limpeza.getProximaVez());
  }
}
