package com.iagora.backend.modules.casa.cozinha.receitaItem;

import com.iagora.backend.exception.RegraNegocioException;
import com.iagora.backend.modules.casa.cozinha.produto.ProdutoRepository;
import com.iagora.backend.modules.casa.cozinha.receita.Receita;
import com.iagora.backend.modules.casa.cozinha.receita.ReceitaRepository;

import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class ReceitaItemService {

  private final ReceitaItemRepository receitaItemRepository;
  private final ReceitaRepository receitaRepository;
  private final ProdutoRepository produtoRepository;

  public ReceitaItemService(ReceitaItemRepository receitaItemRepository,
    ReceitaRepository receitaRepository, ProdutoRepository produtoRepository) {
    this.receitaItemRepository = receitaItemRepository;
    this.receitaRepository = receitaRepository;
    this.produtoRepository = produtoRepository;
  }

  @Transactional
  public ReceitaItemResponse cadastrar(Integer receitaId, ReceitaItemRequest request) {
    ReceitaItem item = new ReceitaItem();
    item.setReceita(buscarReceita(receitaId));
    aplicar(item, request);
    return resposta(receitaItemRepository.save(item));
  }

  @Transactional
  public List<ReceitaItemResponse> listar(Integer receitaId) {
    buscarReceita(receitaId);
    return receitaItemRepository.findByReceita_Id(receitaId).stream().map(this::resposta).toList();
  }

  @Transactional
  public ReceitaItemResponse buscarPorId(Integer receitaId, Integer id) {
    return resposta(buscar(receitaId, id));
  }

  @Transactional
  public void atualizar(Integer receitaId, Integer id, ReceitaItemRequest request) {
    ReceitaItem item = buscar(receitaId, id);
    aplicar(item, request);
    receitaItemRepository.save(item);
  }

  @Transactional
  public void apagar(Integer receitaId, Integer id) {
    receitaItemRepository.delete(buscar(receitaId, id));
  }

  private Receita buscarReceita(Integer id) {
    return receitaRepository.findById(id).orElseThrow(() ->
      new RegraNegocioException("Receita não encontrada", HttpStatus.NOT_FOUND)
    );
  }

  private ReceitaItem buscar(Integer receitaId, Integer id) {
    return receitaItemRepository.findByIdAndReceita_Id(id, receitaId).orElseThrow(() ->
      new RegraNegocioException("Ingrediente não encontrado nesta receita", HttpStatus.NOT_FOUND)
    );
  }

  private void aplicar(ReceitaItem item, ReceitaItemRequest request) {
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

    if (item.getProduto() == null && (item.getNomeItem() == null || item.getNomeItem().isBlank())) {
      throw new RegraNegocioException("Informe um produto ou o nome do ingrediente", HttpStatus.BAD_REQUEST);
    }
    if (item.getQuantidade() == null || item.getQuantidade().signum() <= 0) {
      throw new RegraNegocioException("Quantidade deve ser maior que zero", HttpStatus.BAD_REQUEST);
    }
  }

  private ReceitaItemResponse resposta(ReceitaItem item) {
    return new ReceitaItemResponse(item.getId(), item.getReceita().getId(),
      item.getProduto() == null ? null : item.getProduto().getId(),
      item.getProduto() == null ? item.getNomeItem() : item.getProduto().getNome(), item.getQuantidade());
  }
}
