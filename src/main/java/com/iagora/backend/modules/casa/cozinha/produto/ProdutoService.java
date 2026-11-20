package com.iagora.backend.modules.casa.cozinha.produto;

import com.iagora.backend.exception.RegraNegocioException;
import com.iagora.backend.modules.casa.cozinha.compraItem.CompraItemRepository;
import com.iagora.backend.modules.casa.cozinha.estoqueItem.EstoqueItemRepository;
import com.iagora.backend.modules.casa.cozinha.receitaItem.ReceitaItemRepository;

import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class ProdutoService {

  private final ProdutoRepository produtoRepository;
  private final CompraItemRepository compraItemRepository;
  private final EstoqueItemRepository estoqueItemRepository;
  private final ReceitaItemRepository receitaItemRepository;

  public ProdutoService(
    ProdutoRepository produtoRepository,
    CompraItemRepository compraItemRepository,
    EstoqueItemRepository estoqueItemRepository,
    ReceitaItemRepository receitaItemRepository
  ) {
    this.produtoRepository = produtoRepository;
    this.compraItemRepository = compraItemRepository;
    this.estoqueItemRepository = estoqueItemRepository;
    this.receitaItemRepository = receitaItemRepository;
  }

  public ProdutoResponse cadastrar(ProdutoRequest request) {
    return cadastrar(request, null);
  }

  public ProdutoResponse cadastrar(ProdutoRequest request, Integer userId) {
    Produto produto = new Produto();
    produto.setUserId(userId);
    produto.setNome(request.nome());
    produto.setMarca(request.marca());
    produto.setTamanho(request.tamanho());
    produto.setUnidade(request.unidade());
    produto.setImagem(request.imagem());
    validar(produto);
    return resposta(produtoRepository.save(produto));
  }

  public List<ProdutoResponse> listar() {
    return produtoRepository.findAll().stream().map(this::resposta).toList();
  }

  public ProdutoResponse buscarPorId(Integer id) {
    return resposta(buscar(id));
  }

  public List<ProdutoResponse> buscarPorNome(String nome) {
    if (nome.isBlank()) {
      throw new RegraNegocioException("Nome é obrigatório para a busca", HttpStatus.BAD_REQUEST);
    }
    return produtoRepository.findByNomeContainingIgnoreCase(nome).stream()
      .map(this::resposta).toList();
  }

  @Transactional
  public void atualizar(Integer id, ProdutoRequest request) {
    Produto produto = buscar(id);
    if (request.nome() != null) {
      produto.setNome(request.nome());
    }
    if (request.marca() != null) {
      produto.setMarca(request.marca());
    }
    if (request.tamanho() != null) {
      produto.setTamanho(request.tamanho());
    }
    if (request.unidade() != null) {
      produto.setUnidade(request.unidade());
    }
    if (request.imagem() != null) {
      produto.setImagem(request.imagem());
    }
    validar(produto);
    produtoRepository.save(produto);
  }

  @Transactional
  public void apagar(Integer id) {
    Produto produto = buscar(id);
    if (compraItemRepository.existsByProduto_Id(id) ||
        estoqueItemRepository.existsByProduto_Id(id) ||
        receitaItemRepository.existsByProduto_Id(id)) {
      throw new RegraNegocioException("Produto está sendo utilizado", HttpStatus.CONFLICT);
    }
    produtoRepository.delete(produto);
  }

  public boolean pertenceAoUsuario(Integer id, Integer userId) {
    return userId.equals(buscar(id).getUserId());
  }

  private Produto buscar(Integer id) {
    return produtoRepository.findById(id).orElseThrow(() ->
      new RegraNegocioException("Produto não encontrado", HttpStatus.NOT_FOUND)
    );
  }

  private void validar(Produto produto) {
    if (produto.getNome() == null || produto.getNome().isBlank()) {
      throw new RegraNegocioException("Nome é obrigatório", HttpStatus.BAD_REQUEST);
    }
    if (produto.getTamanho() != null && produto.getTamanho().signum() <= 0) {
      throw new RegraNegocioException("Tamanho deve ser maior que zero", HttpStatus.BAD_REQUEST);
    }
    if (produto.getUnidade() != null && produto.getUnidade().isBlank()) {
      throw new RegraNegocioException("Unidade não pode ficar em branco", HttpStatus.BAD_REQUEST);
    }
  }

  private ProdutoResponse resposta(Produto produto) {
    return new ProdutoResponse(produto.getId(), produto.getNome(), produto.getMarca(),
      produto.getTamanho(), produto.getUnidade(), produto.getImagem());
  }
}
