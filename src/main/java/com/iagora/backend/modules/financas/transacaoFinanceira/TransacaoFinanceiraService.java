package com.iagora.backend.modules.financas.transacaoFinanceira;

import com.iagora.backend.exception.RegraNegocioException;
import com.iagora.backend.modules.financas.categoriaFinancas.CategoriaFinancasService;
import com.iagora.backend.modules.financas.contaFinanceira.ContaFinanceiraService;
import com.iagora.backend.modules.financas.recorrenciaFinanceira.RecorrenciaFinanceiraService;
import com.iagora.backend.modules.identidade.autenticacao.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class TransacaoFinanceiraService {
    private final TransacaoFinanceiraRepository repository;
    private final EntityManager entityManager;
    private final UserRepository userRepository;
    private final CategoriaFinancasService categoriaService;
    private final ContaFinanceiraService contaService;
    private final RecorrenciaFinanceiraService recorrenciaService;

    public TransacaoFinanceiraService(EntityManager entityManager, TransacaoFinanceiraRepository repository,
                                      UserRepository userRepository, CategoriaFinancasService categoriaService,
                                      ContaFinanceiraService contaService,
                                      RecorrenciaFinanceiraService recorrenciaService) {
        this.repository = repository;
        this.entityManager = entityManager;
        this.userRepository = userRepository;
        this.categoriaService = categoriaService;
        this.contaService = contaService;
        this.recorrenciaService = recorrenciaService;
    }

    public Integer proprietarioId(TransacaoFinanceira item) {
        return item.getUser().getId();
    }

    @Transactional
    public TransacaoFinanceira buscarDoUsuario(Integer id, Integer userId) {
        TransacaoFinanceira item = repository.findById(id).orElseThrow(() -> new RegraNegocioException("Registro não encontrado", HttpStatus.NOT_FOUND));
        if (!proprietarioId(item).equals(userId)) {
            throw new RegraNegocioException("Você não tem permissão para acessar este recurso", HttpStatus.FORBIDDEN);
        }
        return item;
    }

    @Transactional
    public TransacaoFinanceiraResponse cadastrar(TransacaoFinanceiraRequest request, Integer userId) {
        TransacaoFinanceira item = new TransacaoFinanceira();
        if (request.userId() != null && !request.userId().equals(userId)) {
            throw new RegraNegocioException("Usuário inválido para esta operação", HttpStatus.FORBIDDEN);
        }
        item.setUser(userRepository.findById(userId).orElseThrow(() -> new RegraNegocioException("Registro não encontrado", HttpStatus.NOT_FOUND)));
        aplicar(item, request, userId);
        return resposta(repository.save(item));
    }

    @Transactional
    public TransacaoFinanceiraResponse buscarPorId(Integer id, Integer userId) {
        return resposta(buscarDoUsuario(id, userId));
    }

    @Transactional
    public List<TransacaoFinanceiraResponse> listarPorUsuario(Integer userId) {
        return repository.findByUser_IdOrderByIdAsc(userId).stream().map(this::resposta).toList();
    }

    @Transactional
    public void atualizar(Integer id, TransacaoFinanceiraRequest request, Integer userId) {
        aplicar(buscarDoUsuario(id, userId), request, userId);
    }

    @Transactional
    public void apagar(Integer id, Integer userId) {
        TransacaoFinanceira item = buscarDoUsuario(id, userId);
        repository.delete(item);
    }

    private void aplicar(TransacaoFinanceira item, TransacaoFinanceiraRequest request, Integer userId) {
        if (request.categoriaId() != null) {
            var categoria = categoriaService.buscarDoUsuario(request.categoriaId(), userId);
            entityManager.lock(categoria, LockModeType.PESSIMISTIC_WRITE);
            item.setCategoria(categoria);
        }
        if (item.getCategoria() == null) {
            throw new RegraNegocioException("Campo categoriaId obrigatório", HttpStatus.BAD_REQUEST);
        }
        if (request.contaId() != null) {
            item.setConta(contaService.buscarDoUsuario(request.contaId(), userId));
        }
        if (request.recorrenciaId() != null) {
            item.setRecorrencia(recorrenciaService.buscarDoUsuario(request.recorrenciaId(), userId));
        }
        if (request.descricao() != null) {
            if (request.descricao().isBlank()) {
                throw new RegraNegocioException("Campo descricao inválido", HttpStatus.BAD_REQUEST);
            }
            item.setDescricao(request.descricao().strip());
        }
        if (item.getDescricao() == null) {
            throw new RegraNegocioException("Campo descricao obrigatório", HttpStatus.BAD_REQUEST);
        }
        if (request.tipo() != null) {
            if (request.tipo().isBlank() || request.tipo().strip().length() > 30) {
                throw new RegraNegocioException("Campo tipo inválido", HttpStatus.BAD_REQUEST);
            }
            if (!List.of("receita", "despesa").contains(request.tipo())) {
                throw new RegraNegocioException("Campo tipo inválido", HttpStatus.BAD_REQUEST);
            }
            item.setTipo(request.tipo().strip());
        }
        if (item.getTipo() == null) {
            throw new RegraNegocioException("Campo tipo obrigatório", HttpStatus.BAD_REQUEST);
        }
        if (request.valor() != null) {
            if (request.valor().scale() > 2 || request.valor().precision() - request.valor().scale() > 13 || request.valor().signum() < 0) {
                throw new RegraNegocioException("Campo valor inválido", HttpStatus.BAD_REQUEST);
            }
            item.setValor(request.valor());
        }
        if (item.getValor() == null) {
            throw new RegraNegocioException("Campo valor obrigatório", HttpStatus.BAD_REQUEST);
        }
        if (request.competencia() != null) {
            item.setCompetencia(request.competencia());
        }
        if (item.getCompetencia() == null) {
            throw new RegraNegocioException("Campo competencia obrigatório", HttpStatus.BAD_REQUEST);
        }
        if (request.vencimento() != null) {
            item.setVencimento(request.vencimento());
        }
    }

    private TransacaoFinanceiraResponse resposta(TransacaoFinanceira item) {
        return new TransacaoFinanceiraResponse(
            item.getId(),
            item.getUser().getId(),
            item.getCategoria().getId(),
            item.getConta() == null ? null : item.getConta().getId(),
            item.getRecorrencia() == null ? null : item.getRecorrencia().getId(),
            item.getDescricao(),
            item.getTipo(),
            item.getValor(),
            item.getCompetencia(),
            item.getVencimento(),
            item.getPagoEm(),
            item.getStatus()
        );
    }

    @Transactional
    public void pagar(Integer id, Integer userId, PagarTransacaoRequest data) {
        TransacaoFinanceira item = buscarDoUsuario(id, userId);
        item.setStatus("pago");
        if (data != null && data.pagoEm() != null) {
            item.setPagoEm(data.pagoEm());
        } else if (item.getPagoEm() == null) {
            item.setPagoEm(LocalDate.now());
        }
    }

    @Transactional
    public void pendente(Integer id, Integer userId) {
        TransacaoFinanceira item = buscarDoUsuario(id, userId);
        item.setStatus("pendente");
        item.setPagoEm(null);
    }

    @Transactional
    public ResumoFinanceiroResponse resumo(Integer userId) {
        List<TransacaoFinanceira> itens = repository.findByUser_IdOrderByIdAsc(userId);
        BigDecimal receitas = total(itens, "receita", "pago");
        BigDecimal despesas = total(itens, "despesa", "pago");
        return new ResumoFinanceiroResponse(receitas, despesas, receitas.subtract(despesas),
            total(itens, "receita", "pendente"), total(itens, "despesa", "pendente"));
    }

    private BigDecimal total(List<TransacaoFinanceira> itens, String tipo, String status) {
        return itens.stream()
            .filter(item -> tipo.equals(item.getTipo()) && status.equals(item.getStatus()))
            .map(TransacaoFinanceira::getValor)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
