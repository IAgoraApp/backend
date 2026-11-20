package com.iagora.backend.modules.casa.cozinha.compras;

import com.iagora.backend.exception.RegraNegocioException;
import com.iagora.backend.modules.casa.casa.CasaRepository;
import com.iagora.backend.modules.casa.cozinha.compraItem.CompraItemRepository;
import com.iagora.backend.modules.casa.cozinha.compraItem.CompraItemService;

import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class ComprasService {

  private final ComprasRepository comprasRepository;
  private final CasaRepository casaRepository;
  private final CompraItemRepository compraItemRepository;
  private final CompraItemService itens;

  public ComprasService(ComprasRepository comprasRepository, CasaRepository casaRepository,
    CompraItemRepository compraItemRepository, CompraItemService itens) {
    this.comprasRepository = comprasRepository;
    this.casaRepository = casaRepository;
    this.compraItemRepository = compraItemRepository;
    this.itens = itens;
  }

  @Transactional
  public ComprasResponse cadastrar(ComprasRequest request) {
    if (request.casaId() == null) {
      throw new RegraNegocioException("Casa é obrigatória", HttpStatus.BAD_REQUEST);
    }
    validarNome(request.nome());
    Compras compras = new Compras();
    compras.setCasa(casaRepository.findById(request.casaId()).orElseThrow(() ->
      new RegraNegocioException("Casa não encontrada", HttpStatus.NOT_FOUND)
    ));
    compras.setNome(request.nome());
    compras = comprasRepository.save(compras);
    if (request.itens() != null) {
      if (request.itens().size() > 100 || request.itens().stream().anyMatch(java.util.Objects::isNull)) {
        throw new RegraNegocioException("Informe até 100 itens válidos", HttpStatus.BAD_REQUEST);
      }
      for (var item : request.itens()) itens.cadastrar(compras.getId(), item);
    }
    return resposta(compras);
  }

  @Transactional
  public ComprasResponse buscarPorId(Integer id) {
    return resposta(buscar(id));
  }

  @Transactional
  public List<ComprasResponse> buscarPorCasaId(Integer casaId) {
    if (!casaRepository.existsById(casaId)) {
      throw new RegraNegocioException("Casa não encontrada", HttpStatus.NOT_FOUND);
    }
    return comprasRepository.findByCasa_Id(casaId).stream().map(this::resposta).toList();
  }

  @Transactional
  public void atualizar(Integer id, ComprasRequest request) {
    Compras compras = buscar(id);
    if (request.nome() != null) {
      validarNome(request.nome());
      compras.setNome(request.nome());
    }
    comprasRepository.save(compras);
  }

  @Transactional
  public void apagar(Integer id) {
    Compras compras = buscar(id);
    compraItemRepository.deleteByCompras_Id(id);
    comprasRepository.delete(compras);
  }

  private Compras buscar(Integer id) {
    return comprasRepository.findById(id).orElseThrow(() ->
      new RegraNegocioException("Lista de compras não encontrada", HttpStatus.NOT_FOUND)
    );
  }

  private void validarNome(String nome) {
    if (nome == null || nome.isBlank()) {
      throw new RegraNegocioException("Nome é obrigatório", HttpStatus.BAD_REQUEST);
    }
  }

  private ComprasResponse resposta(Compras compras) {
    return new ComprasResponse(compras.getId(), compras.getCasa().getId(),
      compras.getNome(), compras.getCriacao());
  }
}
