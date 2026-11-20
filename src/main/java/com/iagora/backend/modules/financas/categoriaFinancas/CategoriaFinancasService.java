package com.iagora.backend.modules.financas.categoriaFinancas;

import com.iagora.backend.exception.RegraNegocioException;
import com.iagora.backend.modules.financas.orcamento.OrcamentoRepository;
import com.iagora.backend.modules.financas.transacaoFinanceira.TransacaoFinanceiraRepository;
import com.iagora.backend.modules.identidade.autenticacao.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class CategoriaFinancasService {
    private final CategoriaFinancasRepository repository;
    private final EntityManager entityManager;
    private final OrcamentoRepository orcamentoRepository;
    private final TransacaoFinanceiraRepository transacaoRepository;
    private final UserRepository userRepository;

    public CategoriaFinancasService(OrcamentoRepository orcamentoRepository,
                                    TransacaoFinanceiraRepository transacaoRepository, EntityManager entityManager,
                                    CategoriaFinancasRepository repository, UserRepository userRepository) {
        this.repository = repository;
        this.orcamentoRepository = orcamentoRepository;
        this.transacaoRepository = transacaoRepository;
        this.entityManager = entityManager;
        this.userRepository = userRepository;
    }

    public Integer proprietarioId(CategoriaFinancas item) {
        return item.getUser().getId();
    }

    @Transactional
    public CategoriaFinancas buscarDoUsuario(Integer id, Integer userId) {
        CategoriaFinancas item = repository.findById(id).orElseThrow(() -> new RegraNegocioException("Registro não encontrado", HttpStatus.NOT_FOUND));
        if (!proprietarioId(item).equals(userId)) {
            throw new RegraNegocioException("Você não tem permissão para acessar este recurso", HttpStatus.FORBIDDEN);
        }
        return item;
    }

    @Transactional
    public CategoriaFinancasResponse cadastrar(CategoriaFinancasRequest request, Integer userId) {
        CategoriaFinancas item = new CategoriaFinancas();
        if (request.userId() != null && !request.userId().equals(userId)) {
            throw new RegraNegocioException("Usuário inválido para esta operação", HttpStatus.FORBIDDEN);
        }
        item.setUser(userRepository.findById(userId).orElseThrow(() -> new RegraNegocioException("Registro não encontrado", HttpStatus.NOT_FOUND)));
        aplicar(item, request, userId);
        return resposta(repository.save(item));
    }

    @Transactional
    public CategoriaFinancasResponse buscarPorId(Integer id, Integer userId) {
        return resposta(buscarDoUsuario(id, userId));
    }

    @Transactional
    public List<CategoriaFinancasResponse> listarPorUsuario(Integer userId) {
        return repository.findByUser_IdOrderByIdAsc(userId).stream().map(this::resposta).toList();
    }

    @Transactional
    public void atualizar(Integer id, CategoriaFinancasRequest request, Integer userId) {
        aplicar(buscarDoUsuario(id, userId), request, userId);
    }

    @Transactional
    public void apagar(Integer id, Integer userId) {
        CategoriaFinancas item = buscarDoUsuario(id, userId);
        entityManager.lock(item, LockModeType.PESSIMISTIC_WRITE);
        if (orcamentoRepository.existsByCategoria_Id(id) || transacaoRepository.existsByCategoria_Id(id)) {
            throw new RegraNegocioException("A categoria possui registros financeiros vinculados", HttpStatus.CONFLICT);
        }
        repository.delete(item);
    }

    private void aplicar(CategoriaFinancas item, CategoriaFinancasRequest request, Integer userId) {
        if (request.nome() != null) {
            if (request.nome().isBlank() || request.nome().strip().length() > 100) {
                throw new RegraNegocioException("Campo nome inválido", HttpStatus.BAD_REQUEST);
            }
            if (repository.existsByUser_IdAndNomeIgnoreCaseAndIdNot(userId, request.nome().strip(), item.getId() == null ? 0 : item.getId())) {
                throw new RegraNegocioException("Categoria financeira já cadastrada", HttpStatus.CONFLICT);
            }
            item.setNome(request.nome().strip());
        }
        if (item.getNome() == null) {
            throw new RegraNegocioException("Campo nome obrigatório", HttpStatus.BAD_REQUEST);
        }
        if (request.cor() != null) {
            if (request.cor().strip().length() > 20) {
                throw new RegraNegocioException("Campo cor inválido", HttpStatus.BAD_REQUEST);
            }
            item.setCor(request.cor().strip());
        }
        if (request.icone() != null) {
            if (request.icone().strip().length() > 100) {
                throw new RegraNegocioException("Campo icone inválido", HttpStatus.BAD_REQUEST);
            }
            item.setIcone(request.icone().strip());
        }
    }

    private CategoriaFinancasResponse resposta(CategoriaFinancas item) {
        return new CategoriaFinancasResponse(
            item.getId(),
            item.getUser().getId(),
            item.getNome(),
            item.getCor(),
            item.getIcone()
        );
    }
}
