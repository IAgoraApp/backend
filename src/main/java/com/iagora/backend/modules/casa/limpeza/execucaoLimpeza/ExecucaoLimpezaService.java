package com.iagora.backend.modules.casa.limpeza.execucaoLimpeza;

import com.iagora.backend.exception.RegraNegocioException;
import com.iagora.backend.modules.casa.limpeza.limpeza.Limpeza;
import com.iagora.backend.modules.casa.limpeza.limpeza.LimpezaRepository;
import com.iagora.backend.modules.identidade.perfil.Perfil;
import com.iagora.backend.modules.identidade.perfil.PerfilRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class ExecucaoLimpezaService {

  private final ExecucaoLimpezaRepository execucaoLimpezaRepository;
  private final LimpezaRepository limpezaRepository;
  private final PerfilRepository perfilRepository;

  public ExecucaoLimpezaService(ExecucaoLimpezaRepository execucaoLimpezaRepository,
    LimpezaRepository limpezaRepository, PerfilRepository perfilRepository) {
    this.execucaoLimpezaRepository = execucaoLimpezaRepository;
    this.limpezaRepository = limpezaRepository;
    this.perfilRepository = perfilRepository;
  }

  @Transactional
  public ExecucaoLimpezaResponse cadastrar(Integer limpezaId, ExecucaoLimpezaRequest request) {
    Limpeza limpeza = buscarLimpeza(limpezaId);
    if (request.perfilId() == null) {
      throw new RegraNegocioException("Perfil é obrigatório", HttpStatus.BAD_REQUEST);
    }
    Perfil perfil = perfilRepository.findById(request.perfilId()).orElseThrow(() ->
      new RegraNegocioException("Perfil não encontrado", HttpStatus.NOT_FOUND)
    );
    if (!perfil.getUser().getId().equals(limpeza.getComodo().getCasa().getUser().getId())) {
      throw new RegraNegocioException("Perfil não pertence ao proprietário da casa", HttpStatus.FORBIDDEN);
    }

    ExecucaoLimpeza execucao = new ExecucaoLimpeza();
    execucao.setLimpeza(limpeza);
    execucao.setPerfil(perfil);
    execucao.setRealizadaEm(request.realizadaEm() == null ? LocalDateTime.now() : request.realizadaEm());
    execucao = execucaoLimpezaRepository.save(execucao);
    atualizarUltimaVez(limpeza);
    return resposta(execucao);
  }

  @Transactional
  public List<ExecucaoLimpezaResponse> listar(Integer limpezaId) {
    buscarLimpeza(limpezaId);
    return execucaoLimpezaRepository.findByLimpeza_IdOrderByRealizadaEmDescIdDesc(limpezaId)
      .stream().map(this::resposta).toList();
  }

  @Transactional
  public ExecucaoLimpezaResponse buscarPorId(Integer limpezaId, Integer id) {
    return resposta(buscar(limpezaId, id));
  }

  @Transactional
  public List<ExecucaoLimpezaResponse> buscarPorPerfilId(Integer perfilId) {
    if (!perfilRepository.existsById(perfilId)) {
      throw new RegraNegocioException("Perfil não encontrado", HttpStatus.NOT_FOUND);
    }
    return execucaoLimpezaRepository.findByPerfil_IdOrderByRealizadaEmDescIdDesc(perfilId)
      .stream().map(this::resposta).toList();
  }

  @Transactional
  public void apagar(Integer limpezaId, Integer id) {
    ExecucaoLimpeza execucao = buscar(limpezaId, id);
    Limpeza limpeza = execucao.getLimpeza();
    execucaoLimpezaRepository.delete(execucao);
    atualizarUltimaVez(limpeza);
  }

  private Limpeza buscarLimpeza(Integer id) {
    return limpezaRepository.findById(id).orElseThrow(() ->
      new RegraNegocioException("Limpeza não encontrada", HttpStatus.NOT_FOUND)
    );
  }

  private ExecucaoLimpeza buscar(Integer limpezaId, Integer id) {
    return execucaoLimpezaRepository.findByIdAndLimpeza_Id(id, limpezaId).orElseThrow(() ->
      new RegraNegocioException("Execução não encontrada nesta limpeza", HttpStatus.NOT_FOUND)
    );
  }

  private void atualizarUltimaVez(Limpeza limpeza) {
    limpeza.setUltimaVez(execucaoLimpezaRepository
      .findFirstByLimpeza_IdOrderByRealizadaEmDescIdDesc(limpeza.getId())
      .map(execucao -> execucao.getRealizadaEm().toLocalDate())
      .orElse(null));
    limpezaRepository.save(limpeza);
  }

  private ExecucaoLimpezaResponse resposta(ExecucaoLimpeza execucao) {
    return new ExecucaoLimpezaResponse(execucao.getId(), execucao.getLimpeza().getId(),
      execucao.getPerfil().getId(), execucao.getRealizadaEm());
  }
}
