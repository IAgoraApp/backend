package com.iagora.backend.modules.financas.contaFinanceira;

import com.iagora.backend.exception.RegraNegocioException;
import com.iagora.backend.modules.financas.transacaoFinanceira.TransacaoFinanceiraRepository;
import com.iagora.backend.modules.financas.transacaoFinanceira.TransacaoFinanceiraResponse;
import com.iagora.backend.modules.identidade.autenticacao.UserRepository;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class ContaFinanceiraService {
    private final TransacaoFinanceiraRepository transacaoRepository;
    private final ContaFinanceiraRepository repository;
    private final UserRepository userRepository;

    public ContaFinanceiraService(TransacaoFinanceiraRepository transacaoRepository,
                                  ContaFinanceiraRepository repository, UserRepository userRepository) {
        this.transacaoRepository = transacaoRepository;
        this.repository = repository;
        this.userRepository = userRepository;
    }

    public Integer proprietarioId(ContaFinanceira item) {
        return item.getUser().getId();
    }

    @Transactional
    public ContaFinanceira buscarDoUsuario(Integer id, Integer userId) {
        ContaFinanceira item = repository.findById(id).orElseThrow(() -> new RegraNegocioException("Registro não encontrado", HttpStatus.NOT_FOUND));
        if (!proprietarioId(item).equals(userId)) {
            throw new RegraNegocioException("Você não tem permissão para acessar este recurso", HttpStatus.FORBIDDEN);
        }
        return item;
    }

    @Transactional
    public ContaFinanceiraResponse cadastrar(ContaFinanceiraRequest request, Integer userId) {
        ContaFinanceira item = new ContaFinanceira();
        if (request.userId() != null && !request.userId().equals(userId)) {
            throw new RegraNegocioException("Usuário inválido para esta operação", HttpStatus.FORBIDDEN);
        }
        item.setUser(userRepository.findById(userId).orElseThrow(() -> new RegraNegocioException("Registro não encontrado", HttpStatus.NOT_FOUND)));
        aplicar(item, request, userId);
        return resposta(repository.save(item));
    }

    @Transactional
    public ContaFinanceiraResponse buscarPorId(Integer id, Integer userId) {
        return resposta(buscarDoUsuario(id, userId));
    }

    @Transactional
    public List<ContaFinanceiraResponse> listarPorUsuario(Integer userId) {
        return repository.findByUser_IdOrderByIdAsc(userId).stream().map(this::resposta).toList();
    }

    @Transactional
    public void atualizar(Integer id, ContaFinanceiraRequest request, Integer userId) {
        aplicar(buscarDoUsuario(id, userId), request, userId);
    }

    @Transactional
    public void apagar(Integer id, Integer userId) {
        ContaFinanceira item = buscarDoUsuario(id, userId);
        repository.delete(item);
    }

    private void aplicar(ContaFinanceira item, ContaFinanceiraRequest request, Integer userId) {
        if (request.nome() != null) {
            if (request.nome().isBlank() || request.nome().strip().length() > 150) {
                throw new RegraNegocioException("Campo nome inválido", HttpStatus.BAD_REQUEST);
            }
            item.setNome(request.nome().strip());
        }
        if (item.getNome() == null) {
            throw new RegraNegocioException("Campo nome obrigatório", HttpStatus.BAD_REQUEST);
        }
        if (request.tipo() != null) {
            if (request.tipo().isBlank() || request.tipo().strip().length() > 50) {
                throw new RegraNegocioException("Campo tipo inválido", HttpStatus.BAD_REQUEST);
            }
            item.setTipo(request.tipo().strip());
        }
        if (item.getTipo() == null) {
            throw new RegraNegocioException("Campo tipo obrigatório", HttpStatus.BAD_REQUEST);
        }
        if (request.saldoInicial() != null) {
            if (request.saldoInicial().scale() > 2 || request.saldoInicial().precision() - request.saldoInicial().scale() > 13) {
                throw new RegraNegocioException("Campo saldoInicial inválido", HttpStatus.BAD_REQUEST);
            }
            item.setSaldoInicial(request.saldoInicial());
        }
    }

    private ContaFinanceiraResponse resposta(ContaFinanceira item) {
        return new ContaFinanceiraResponse(
            item.getId(),
            item.getUser().getId(),
            item.getNome(),
            item.getTipo(),
            item.getSaldoInicial(),
            saldoAtual(item)
        );
    }

    @Transactional
    public List<TransacaoFinanceiraResponse> extrato(Integer id, Integer userId) {
        buscarDoUsuario(id, userId);
        return transacaoRepository.findByConta_IdOrderByIdAsc(id).stream()
            .map(item -> new TransacaoFinanceiraResponse(item.getId(), userId, item.getCategoria().getId(),
                id, item.getRecorrencia() == null ? null : item.getRecorrencia().getId(), item.getDescricao(),
                item.getTipo(), item.getValor(), item.getCompetencia(), item.getVencimento(), item.getPagoEm(),
                item.getStatus()))
            .toList();
    }

    private BigDecimal saldoAtual(ContaFinanceira conta) {
        return transacaoRepository.findByConta_IdOrderByIdAsc(conta.getId()).stream()
            .filter(item -> "pago".equals(item.getStatus()))
            .map(item -> "receita".equals(item.getTipo()) ? item.getValor() : item.getValor().negate())
            .reduce(conta.getSaldoInicial(), BigDecimal::add);
    }
}
