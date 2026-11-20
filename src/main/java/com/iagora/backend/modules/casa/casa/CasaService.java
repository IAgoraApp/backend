package com.iagora.backend.modules.casa.casa;

import com.iagora.backend.exception.RegraNegocioException;
import com.iagora.backend.modules.casa.cozinha.compraItem.CompraItemRepository;
import com.iagora.backend.modules.casa.cozinha.compras.ComprasRepository;
import com.iagora.backend.modules.casa.cozinha.estoqueItem.EstoqueItemRepository;
import com.iagora.backend.modules.endereco.EnderecoService;
import com.iagora.backend.modules.endereco.EnderecoResponse;
import com.iagora.backend.modules.casa.limpeza.comodo.ComodoRepository;
import com.iagora.backend.modules.casa.limpeza.limpeza.LimpezaRepository;
import com.iagora.backend.modules.casa.limpeza.execucaoLimpeza.ExecucaoLimpezaRepository;
import com.iagora.backend.modules.identidade.autenticacao.User;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class CasaService {

  private final CasaRepository casaRepository;
  private final EnderecoService enderecoService;
  private final ComprasRepository comprasRepository;
  private final CompraItemRepository compraItemRepository;
  private final EstoqueItemRepository estoqueItemRepository;
  private final ComodoRepository comodoRepository;
  private final LimpezaRepository limpezaRepository;
  private final ExecucaoLimpezaRepository execucaoLimpezaRepository;

  public CasaService(CasaRepository casaRepository, EnderecoService enderecoService,
    ComprasRepository comprasRepository, CompraItemRepository compraItemRepository,
    EstoqueItemRepository estoqueItemRepository, ComodoRepository comodoRepository,
    LimpezaRepository limpezaRepository, ExecucaoLimpezaRepository execucaoLimpezaRepository) {
    this.casaRepository = casaRepository;
    this.enderecoService = enderecoService;
    this.comprasRepository = comprasRepository;
    this.compraItemRepository = compraItemRepository;
    this.estoqueItemRepository = estoqueItemRepository;
    this.comodoRepository = comodoRepository;
    this.limpezaRepository = limpezaRepository;
    this.execucaoLimpezaRepository = execucaoLimpezaRepository;
  }

  public void criar(User user) {
    if (casaRepository.existsByUser_Id(user.getId())) {
      throw new RegraNegocioException("Usuário já possui casa", HttpStatus.CONFLICT);
    }

    Casa casa = new Casa(user, "Minha casa", "Casa");
    casaRepository.save(casa);
  }

  @Transactional
  public CasaResponse buscarPorId(Integer id) {
    Casa casa = casaRepository
      .findById(id)
      .orElseThrow(() ->
        new RegraNegocioException("Casa não encontrada", HttpStatus.NOT_FOUND)
      );

    return new CasaResponse(
      casa.getId(), casa.getUser().getId(), casa.getNome(), casa.getTipo(), casa.getEnderecoId()
    );
  }

  @Transactional
  public List<CasaResponse> buscarPorUserId(Integer userId) {
    return casaRepository.findByUser_Id(userId).stream()
      .map(casa -> new CasaResponse(
        casa.getId(), casa.getUser().getId(), casa.getNome(), casa.getTipo(), casa.getEnderecoId()
      ))
      .toList();
  }

  @Transactional
  public void atualizar(Integer id, CasaRequest request) {
    Casa casa = casaRepository
      .findById(id)
      .orElseThrow(() ->
        new RegraNegocioException("Casa não encontrada", HttpStatus.NOT_FOUND)
      );

    aplicarIdentidade(casa, request.nome(), request.tipo());

    if (request.enderecoId() != null) {
      enderecoService.buscarPorId(request.enderecoId());
      casa.setEnderecoId(request.enderecoId());
    }

    casaRepository.save(casa);
  }

  @Transactional
  public void atualizarVinculo(Integer id, CasaRequest request) {
    var casa = buscarPorId(id);
    if (request.enderecoId() != null && !request.enderecoId().equals(casa.enderecoId())) {
      enderecoService.verificarProprietario(request.enderecoId(), casa.userId());
    }
    atualizar(id, request);
  }

  @Transactional
  public EnderecoResponse consultarEndereco(Integer id) {
    Integer enderecoId = buscarPorId(id).enderecoId();
    if (enderecoId == null) {
      throw new RegraNegocioException("Residência sem endereço", HttpStatus.NOT_FOUND);
    }
    return enderecoService.buscarPorId(enderecoId);
  }

  @Transactional
  public void editarResidencia(Integer id, ResidenciaRequest request) {
    Casa casa = casaRepository.findCasaById(id).orElseThrow(() ->
      new RegraNegocioException("Casa não encontrada", HttpStatus.NOT_FOUND));
    aplicarIdentidade(casa, request.nome(), request.tipo());
    if (request.endereco() != null) {
      casa.setEnderecoId(enderecoService.salvarResidencia(
        casa.getEnderecoId(), casa.getUser().getId(), request.endereco()
      ));
    }
    casaRepository.save(casa);
  }

  private void aplicarIdentidade(Casa casa, String nome, String tipo) {
    if (nome != null) {
      if (nome.isBlank()) {
        throw new RegraNegocioException("Nome é obrigatório", HttpStatus.BAD_REQUEST);
      }
      casa.setNome(nome);
    }

    if (tipo != null) {
      if (tipo.isBlank()) {
        throw new RegraNegocioException("Tipo é obrigatório", HttpStatus.BAD_REQUEST);
      }
      casa.setTipo(tipo);
    }
  }

  @Transactional
  public void apagarPorUserId(Integer userId) {
    execucaoLimpezaRepository.deleteByLimpeza_Comodo_Casa_User_Id(userId);
    limpezaRepository.deleteByComodo_Casa_User_Id(userId);
    comodoRepository.deleteByCasa_User_Id(userId);
    compraItemRepository.deleteByCompras_Casa_User_Id(userId);
    comprasRepository.deleteByCasa_User_Id(userId);
    estoqueItemRepository.deleteByCasa_User_Id(userId);
    casaRepository.deleteByUser_Id(userId);
  }
}
