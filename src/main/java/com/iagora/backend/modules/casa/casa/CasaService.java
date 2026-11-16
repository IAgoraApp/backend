package com.iagora.backend.modules.casa.casa;

import com.iagora.backend.exception.RegraNegocioException;
import com.iagora.backend.modules.endereco.EnderecoService;
import com.iagora.backend.modules.identidade.autenticacao.User;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class CasaService {

  private final CasaRepository casaRepository;
  private final EnderecoService enderecoService;

  public CasaService(CasaRepository casaRepository, EnderecoService enderecoService) {
    this.casaRepository = casaRepository;
    this.enderecoService = enderecoService;
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

    if (request.nome() != null) {
      if (request.nome().isBlank()) {
        throw new RegraNegocioException("Nome é obrigatório", HttpStatus.BAD_REQUEST);
      }
      casa.setNome(request.nome());
    }

    if (request.tipo() != null) {
      if (request.tipo().isBlank()) {
        throw new RegraNegocioException("Tipo é obrigatório", HttpStatus.BAD_REQUEST);
      }
      casa.setTipo(request.tipo());
    }

    if (request.enderecoId() != null) {
      enderecoService.buscarPorId(request.enderecoId());
      casa.setEnderecoId(request.enderecoId());
    }

    casaRepository.save(casa);
  }

  @Transactional
  public void apagarPorUserId(Integer userId) {
    casaRepository.deleteByUser_Id(userId);
  }
}
