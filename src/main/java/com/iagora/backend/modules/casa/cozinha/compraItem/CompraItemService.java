package com.iagora.backend.modules.casa.cozinha.compraItem;

import com.iagora.backend.exception.RegraNegocioException;
import com.iagora.backend.modules.casa.cozinha.compras.Compras;
import com.iagora.backend.modules.casa.cozinha.compras.ComprasRepository;
import com.iagora.backend.modules.casa.cozinha.produto.ProdutoRepository;

import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class CompraItemService {

  private final CompraItemRepository compraItemRepository;
  private final ComprasRepository comprasRepository;
  private final ProdutoRepository produtoRepository;

  public CompraItemService(CompraItemRepository compraItemRepository,
    ComprasRepository comprasRepository, ProdutoRepository produtoRepository) {
    this.compraItemRepository = compraItemRepository;
    this.comprasRepository = comprasRepository;
    this.produtoRepository = produtoRepository;
  }

  @Transactional
  public CompraItemResponse cadastrar(Integer compraId, CompraItemRequest request) {
    CompraItem item = new CompraItem();
    item.setCompras(buscarCompras(compraId));
    aplicar(item, request);
    return resposta(compraItemRepository.save(item));
  }

  @Transactional
  public List<CompraItemResponse> listar(Integer compraId) {
    buscarCompras(compraId);
    return compraItemRepository.findByCompras_Id(compraId).stream().map(this::resposta).toList();
  }

  @Transactional
  public CompraItemResponse buscarPorId(Integer compraId, Integer id) {
    return resposta(buscar(compraId, id));
  }

  @Transactional
  public void atualizar(Integer compraId, Integer id, CompraItemRequest request) {
    CompraItem item = buscar(compraId, id);
    aplicar(item, request);
    compraItemRepository.save(item);
  }

  @Transactional
  public void marcarComprado(Integer compraId, Integer id, CompradoRequest request) {
    CompraItem item = buscar(compraId, id);
    if (request.comprado() == null) {
      throw new RegraNegocioException("Comprado é obrigatório", HttpStatus.BAD_REQUEST);
    }
    item.setComprado(request.comprado());
    compraItemRepository.save(item);
  }

  @Transactional
  public void apagar(Integer compraId, Integer id) {
    compraItemRepository.delete(buscar(compraId, id));
  }

  private Compras buscarCompras(Integer id) {
    return comprasRepository.findById(id).orElseThrow(() ->
      new RegraNegocioException("Lista de compras não encontrada", HttpStatus.NOT_FOUND)
    );
  }

  private CompraItem buscar(Integer compraId, Integer id) {
    return compraItemRepository.findByIdAndCompras_Id(id, compraId).orElseThrow(() ->
      new RegraNegocioException("Item não encontrado nesta lista de compras", HttpStatus.NOT_FOUND)
    );
  }

  private void aplicar(CompraItem item, CompraItemRequest request) {
    if (request.produtoId() != null) {
      item.setProduto(produtoRepository.findById(request.produtoId()).orElseThrow(() ->
        new RegraNegocioException("Produto não encontrado", HttpStatus.NOT_FOUND)
      ));
    }
    if (request.nomeItem() != null) {
      item.setNomeItem(request.nomeItem());
    }
    if (request.quantidade() != null) {
      item.setQuantidade(request.quantidade());
    }
    if (request.valor() != null) {
      item.setValor(request.valor());
    }

    if (item.getProduto() == null && (item.getNomeItem() == null || item.getNomeItem().isBlank())) {
      throw new RegraNegocioException("Informe um produto ou o nome do item", HttpStatus.BAD_REQUEST);
    }
    if (item.getQuantidade() <= 0) {
      throw new RegraNegocioException("Quantidade deve ser maior que zero", HttpStatus.BAD_REQUEST);
    }
    if (item.getValor() != null && item.getValor().signum() < 0) {
      throw new RegraNegocioException("Valor não pode ser negativo", HttpStatus.BAD_REQUEST);
    }
  }

  private CompraItemResponse resposta(CompraItem item) {
    return new CompraItemResponse(item.getId(), item.getCompras().getId(),
      item.getProduto() == null ? null : item.getProduto().getId(),
      item.getProduto() == null ? item.getNomeItem() : item.getProduto().getNome(),
      item.getQuantidade(), item.getComprado(), item.getValor());
  }
}
