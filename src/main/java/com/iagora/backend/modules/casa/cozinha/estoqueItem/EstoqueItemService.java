package com.iagora.backend.modules.casa.cozinha.estoqueItem;

import com.iagora.backend.exception.RegraNegocioException;
import com.iagora.backend.modules.casa.casa.Casa;
import com.iagora.backend.modules.casa.casa.CasaRepository;
import com.iagora.backend.modules.casa.cozinha.produto.Produto;
import com.iagora.backend.modules.casa.cozinha.produto.ProdutoRepository;

import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class EstoqueItemService {

  private final EstoqueItemRepository estoqueItemRepository;
  private final CasaRepository casaRepository;
  private final ProdutoRepository produtoRepository;

  public EstoqueItemService(EstoqueItemRepository estoqueItemRepository,
    CasaRepository casaRepository, ProdutoRepository produtoRepository) {
    this.estoqueItemRepository = estoqueItemRepository;
    this.casaRepository = casaRepository;
    this.produtoRepository = produtoRepository;
  }

  @Transactional
  public EstoqueItemResponse cadastrar(EstoqueItemRequest request) {
    if (request.casaId() == null || request.produtoId() == null) {
      throw new RegraNegocioException("Casa e produto são obrigatórios", HttpStatus.BAD_REQUEST);
    }
    Casa casa = casaRepository.findById(request.casaId()).orElseThrow(() ->
      new RegraNegocioException("Casa não encontrada", HttpStatus.NOT_FOUND)
    );
    Produto produto = produtoRepository.findById(request.produtoId()).orElseThrow(() ->
      new RegraNegocioException("Produto não encontrado", HttpStatus.NOT_FOUND)
    );
    EstoqueItem item = estoqueItemRepository
      .findByCasa_IdAndProduto_Id(request.casaId(), request.produtoId())
      .orElseGet(EstoqueItem::new);
    item.setCasa(casa);
    item.setProduto(produto);
    if (request.quantidade() != null) {
      item.setQuantidade(request.quantidade());
    }
    validar(item);
    return resposta(estoqueItemRepository.save(item));
  }

  @Transactional
  public EstoqueItemResponse buscarPorId(Integer id) {
    return resposta(buscar(id));
  }

  @Transactional
  public List<EstoqueItemResponse> buscarPorCasaId(Integer casaId) {
    if (!casaRepository.existsById(casaId)) {
      throw new RegraNegocioException("Casa não encontrada", HttpStatus.NOT_FOUND);
    }
    return estoqueItemRepository.findByCasa_Id(casaId).stream().map(this::resposta).toList();
  }

  @Transactional
  public void atualizar(Integer id, EstoqueItemRequest request) {
    EstoqueItem item = buscar(id);
    if (request.quantidade() != null) {
      item.setQuantidade(request.quantidade());
    }
    validar(item);
    estoqueItemRepository.save(item);
  }

  @Transactional
  public void apagar(Integer id) {
    estoqueItemRepository.delete(buscar(id));
  }

  private EstoqueItem buscar(Integer id) {
    return estoqueItemRepository.findById(id).orElseThrow(() ->
      new RegraNegocioException("Item do estoque não encontrado", HttpStatus.NOT_FOUND)
    );
  }

  private void validar(EstoqueItem item) {
    if (item.getQuantidade().signum() < 0) {
      throw new RegraNegocioException("Quantidade não pode ser negativa", HttpStatus.BAD_REQUEST);
    }
  }

  private EstoqueItemResponse resposta(EstoqueItem item) {
    return new EstoqueItemResponse(item.getId(), item.getCasa().getId(),
      item.getProduto().getId(), item.getQuantidade());
  }
}
