package com.iagora.backend.modules.financas.orcamento;

import com.iagora.backend.exception.RegraNegocioException;
import com.iagora.backend.modules.financas.categoriaFinancas.CategoriaFinancasService;
import com.iagora.backend.modules.financas.transacaoFinanceira.TransacaoFinanceira;
import com.iagora.backend.modules.financas.transacaoFinanceira.TransacaoFinanceiraRepository;
import com.iagora.backend.modules.identidade.autenticacao.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class OrcamentoService {
    private final TransacaoFinanceiraRepository transacaoRepository;
    private final OrcamentoRepository repository;
    private final EntityManager entityManager;
    private final UserRepository userRepository;
    private final CategoriaFinancasService categoriaService;

    public OrcamentoService(EntityManager entityManager, TransacaoFinanceiraRepository transacaoRepository,
                            OrcamentoRepository repository, UserRepository userRepository,
                            CategoriaFinancasService categoriaService) {
        this.transacaoRepository = transacaoRepository;
        this.repository = repository;
        this.entityManager = entityManager;
        this.userRepository = userRepository;
        this.categoriaService = categoriaService;
    }

    public Integer proprietarioId(Orcamento item) {
        return item.getUser().getId();
    }

    @Transactional
    public Orcamento buscarDoUsuario(Integer id, Integer userId) {
        Orcamento item = repository.findById(id).orElseThrow(() -> new RegraNegocioException("Registro não encontrado", HttpStatus.NOT_FOUND));
        if (!proprietarioId(item).equals(userId)) {
            throw new RegraNegocioException("Você não tem permissão para acessar este recurso", HttpStatus.FORBIDDEN);
        }
        return item;
    }

    @Transactional
    public OrcamentoResponse cadastrar(OrcamentoRequest request, Integer userId) {
        Orcamento item = new Orcamento();
        if (request.userId() != null && !request.userId().equals(userId)) {
            throw new RegraNegocioException("Usuário inválido para esta operação", HttpStatus.FORBIDDEN);
        }
        item.setUser(userRepository.findById(userId).orElseThrow(() -> new RegraNegocioException("Registro não encontrado", HttpStatus.NOT_FOUND)));
        aplicar(item, request, userId);
        return resposta(repository.save(item));
    }

    @Transactional
    public OrcamentoResponse buscarPorId(Integer id, Integer userId) {
        return resposta(buscarDoUsuario(id, userId));
    }

    @Transactional
    public List<OrcamentoResponse> listarPorUsuario(Integer userId) {
        return repository.findByUser_IdOrderByIdAsc(userId).stream().map(this::resposta).toList();
    }

    @Transactional
    public void atualizar(Integer id, OrcamentoRequest request, Integer userId) {
        aplicar(buscarDoUsuario(id, userId), request, userId);
    }

    @Transactional
    public void apagar(Integer id, Integer userId) {
        Orcamento item = buscarDoUsuario(id, userId);
        repository.delete(item);
    }

    private void aplicar(Orcamento item, OrcamentoRequest request, Integer userId) {
        if (request.categoriaId() != null) {
            var categoria = categoriaService.buscarDoUsuario(request.categoriaId(), userId);
            entityManager.lock(categoria, LockModeType.PESSIMISTIC_WRITE);
            item.setCategoria(categoria);
        }
        if (item.getCategoria() == null) {
            throw new RegraNegocioException("Campo categoriaId obrigatório", HttpStatus.BAD_REQUEST);
        }
        if (request.mes() != null) {
            item.setMes(request.mes().withDayOfMonth(1));
        }
        if (item.getMes() == null) {
            throw new RegraNegocioException("Campo mes obrigatório", HttpStatus.BAD_REQUEST);
        }
        if (request.limite() != null) {
            if (request.limite().scale() > 2 || request.limite().precision() - request.limite().scale() > 13 || request.limite().signum() < 0) {
                throw new RegraNegocioException("Campo limite inválido", HttpStatus.BAD_REQUEST);
            }
            item.setLimite(request.limite());
        }
        if (item.getLimite() == null) {
            throw new RegraNegocioException("Campo limite obrigatório", HttpStatus.BAD_REQUEST);
        }
    }

    private OrcamentoResponse resposta(Orcamento item) {
        return new OrcamentoResponse(
            item.getId(),
            item.getUser().getId(),
            item.getCategoria().getId(),
            item.getMes(),
            item.getLimite()
        );
    }

    @Transactional
    public List<ResumoOrcamentoResponse> resumo(Integer userId) {
        var despesas = transacaoRepository.findByUser_IdOrderByIdAsc(userId).stream()
            .filter(item -> "despesa".equals(item.getTipo()))
            .toList();
        return repository.findByUser_IdOrderByIdAsc(userId).stream().map(orcamento -> {
            Integer categoriaId = orcamento.getCategoria().getId();
            BigDecimal gasto = despesas.stream()
                .filter(item -> item.getCategoria().getId().equals(categoriaId)
                    && item.getCompetencia().withDayOfMonth(1).equals(orcamento.getMes()))
                .map(TransacaoFinanceira::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            return new ResumoOrcamentoResponse(orcamento.getId(), categoriaId, orcamento.getMes(),
                orcamento.getLimite(), gasto, orcamento.getLimite().subtract(gasto));
        }).toList();
    }
}
