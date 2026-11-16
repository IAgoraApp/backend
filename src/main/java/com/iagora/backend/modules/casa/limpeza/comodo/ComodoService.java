package com.iagora.backend.modules.casa.limpeza.comodo;

import com.iagora.backend.exception.RegraNegocioException;
import com.iagora.backend.modules.casa.casa.CasaRepository;
import com.iagora.backend.modules.casa.limpeza.execucaoLimpeza.ExecucaoLimpezaRepository;
import com.iagora.backend.modules.casa.limpeza.limpeza.LimpezaRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class ComodoService {

  private final ComodoRepository comodoRepository;
  private final CasaRepository casaRepository;
  private final LimpezaRepository limpezaRepository;
  private final ExecucaoLimpezaRepository execucaoLimpezaRepository;

  public ComodoService(ComodoRepository comodoRepository, CasaRepository casaRepository,
    LimpezaRepository limpezaRepository, ExecucaoLimpezaRepository execucaoLimpezaRepository) {
    this.comodoRepository = comodoRepository;
    this.casaRepository = casaRepository;
    this.limpezaRepository = limpezaRepository;
    this.execucaoLimpezaRepository = execucaoLimpezaRepository;
  }

  @Transactional
  public ComodoResponse cadastrar(ComodoRequest request) {
    if (request.casaId() == null) {
      throw new RegraNegocioException("Casa é obrigatória", HttpStatus.BAD_REQUEST);
    }
    Comodo comodo = new Comodo();
    comodo.setCasa(casaRepository.findById(request.casaId()).orElseThrow(() ->
      new RegraNegocioException("Casa não encontrada", HttpStatus.NOT_FOUND)
    ));
    aplicar(comodo, request);
    return resposta(comodoRepository.save(comodo));
  }

  @Transactional
  public ComodoResponse buscarPorId(Integer id) {
    return resposta(buscar(id));
  }

  @Transactional
  public List<ComodoResponse> buscarPorCasaId(Integer casaId) {
    if (!casaRepository.existsById(casaId)) {
      throw new RegraNegocioException("Casa não encontrada", HttpStatus.NOT_FOUND);
    }
    return comodoRepository.findByCasa_Id(casaId).stream().map(this::resposta).toList();
  }

  @Transactional
  public void atualizar(Integer id, ComodoRequest request) {
    Comodo comodo = buscar(id);
    aplicar(comodo, request);
    comodoRepository.save(comodo);
  }

  @Transactional
  public void apagar(Integer id) {
    Comodo comodo = buscar(id);
    execucaoLimpezaRepository.deleteByLimpeza_Comodo_Id(id);
    limpezaRepository.deleteByComodo_Id(id);
    comodoRepository.delete(comodo);
  }

  private Comodo buscar(Integer id) {
    return comodoRepository.findById(id).orElseThrow(() ->
      new RegraNegocioException("Cômodo não encontrado", HttpStatus.NOT_FOUND)
    );
  }

  private void aplicar(Comodo comodo, ComodoRequest request) {
    if (request.nome() != null) {
      comodo.setNome(request.nome());
    }
    if (request.tipo() != null) {
      comodo.setTipo(request.tipo());
    }
    if (request.descricao() != null) {
      comodo.setDescricao(request.descricao());
    }
    if (comodo.getNome() == null || comodo.getNome().isBlank() ||
        comodo.getTipo() == null || comodo.getTipo().isBlank()) {
      throw new RegraNegocioException("Nome e tipo são obrigatórios", HttpStatus.BAD_REQUEST);
    }
  }

  private ComodoResponse resposta(Comodo comodo) {
    return new ComodoResponse(comodo.getId(), comodo.getCasa().getId(),
      comodo.getNome(), comodo.getTipo(), comodo.getDescricao());
  }
}
