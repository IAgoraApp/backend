package com.iagora.backend.modules.financas.recorrenciaFinanceira;

import com.iagora.backend.exception.RegraNegocioException;
import com.iagora.backend.modules.financas.categoriaFinancas.CategoriaFinancas;
import com.iagora.backend.modules.financas.categoriaFinancas.CategoriaFinancasRepository;
import com.iagora.backend.modules.financas.transacaoFinanceira.TransacaoFinanceira;
import com.iagora.backend.modules.financas.transacaoFinanceira.TransacaoFinanceiraRepository;
import com.iagora.backend.modules.identidade.autenticacao.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class RecorrenciaFinanceiraService {
    private final CategoriaFinancasRepository categoriaRepository;
    private final TransacaoFinanceiraRepository transacaoRepository;
    private final EntityManager entityManager;
    private final RecorrenciaFinanceiraRepository repository;
    private final UserRepository userRepository;

    public RecorrenciaFinanceiraService(CategoriaFinancasRepository categoriaRepository,
                                        TransacaoFinanceiraRepository transacaoRepository,
                                        EntityManager entityManager, RecorrenciaFinanceiraRepository repository,
                                        UserRepository userRepository) {
        this.categoriaRepository = categoriaRepository;
        this.transacaoRepository = transacaoRepository;
        this.entityManager = entityManager;
        this.repository = repository;
        this.userRepository = userRepository;
    }

    public Integer proprietarioId(RecorrenciaFinanceira item) {
        return item.getUser().getId();
    }

    @Transactional
    public RecorrenciaFinanceira buscarDoUsuario(Integer id, Integer userId) {
        RecorrenciaFinanceira item = repository.findById(id).orElseThrow(() -> new RegraNegocioException("Registro não encontrado", HttpStatus.NOT_FOUND));
        if (!proprietarioId(item).equals(userId)) {
            throw new RegraNegocioException("Você não tem permissão para acessar este recurso", HttpStatus.FORBIDDEN);
        }
        return item;
    }

    @Transactional
    public RecorrenciaFinanceiraResponse cadastrar(RecorrenciaFinanceiraRequest request, Integer userId) {
        RecorrenciaFinanceira item = new RecorrenciaFinanceira();
        if (request.userId() != null && !request.userId().equals(userId)) {
            throw new RegraNegocioException("Usuário inválido para esta operação", HttpStatus.FORBIDDEN);
        }
        item.setUser(userRepository.findById(userId).orElseThrow(() -> new RegraNegocioException("Registro não encontrado", HttpStatus.NOT_FOUND)));
        aplicar(item, request, userId);
        repository.save(item);
        gerar(item, LocalDate.now());
        return resposta(item);
    }

    @Transactional
    public RecorrenciaFinanceiraResponse buscarPorId(Integer id, Integer userId) {
        return resposta(buscarDoUsuario(id, userId));
    }

    @Transactional
    public List<RecorrenciaFinanceiraResponse> listarPorUsuario(Integer userId) {
        return repository.findByUser_IdOrderByIdAsc(userId).stream().map(this::resposta).toList();
    }

    @Transactional
    public void atualizar(Integer id, RecorrenciaFinanceiraRequest request, Integer userId) {
        var item = buscarDoUsuario(id, userId);
        entityManager.refresh(item, LockModeType.PESSIMISTIC_WRITE);
        aplicar(item, request, userId);
        gerar(item, LocalDate.now());
    }

    @Transactional
    public void apagar(Integer id, Integer userId) {
        RecorrenciaFinanceira item = buscarDoUsuario(id, userId);
        entityManager.refresh(item, LockModeType.PESSIMISTIC_WRITE);
        repository.delete(item);
    }

    private void aplicar(RecorrenciaFinanceira item, RecorrenciaFinanceiraRequest request, Integer userId) {
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
        if (request.valorPrevisto() != null) {
            if (request.valorPrevisto().scale() > 2 || request.valorPrevisto().precision() - request.valorPrevisto().scale() > 13 || request.valorPrevisto().signum() < 0) {
                throw new RegraNegocioException("Campo valorPrevisto inválido", HttpStatus.BAD_REQUEST);
            }
            item.setValorPrevisto(request.valorPrevisto());
        }
        if (item.getValorPrevisto() == null) {
            throw new RegraNegocioException("Campo valorPrevisto obrigatório", HttpStatus.BAD_REQUEST);
        }
        if (request.frequencia() != null) {
            if (request.frequencia().isBlank() || request.frequencia().strip().length() > 30) {
                throw new RegraNegocioException("Campo frequencia inválido", HttpStatus.BAD_REQUEST);
            }
            if (!List.of("diaria", "semanal", "mensal", "anual").contains(request.frequencia())) {
                throw new RegraNegocioException("Campo frequencia inválido", HttpStatus.BAD_REQUEST);
            }
            item.setFrequencia(request.frequencia().strip());
        }
        if (item.getFrequencia() == null) {
            throw new RegraNegocioException("Campo frequencia obrigatório", HttpStatus.BAD_REQUEST);
        }
        if (request.inicioEm() != null) {
            item.setInicioEm(request.inicioEm());
        }
        if (item.getInicioEm() == null) {
            throw new RegraNegocioException("Campo inicioEm obrigatório", HttpStatus.BAD_REQUEST);
        }
        if (request.fimEm() != null) {
            item.setFimEm(request.fimEm());
        }
        if (item.getFimEm() != null && item.getFimEm().isBefore(item.getInicioEm())) {
            throw new RegraNegocioException("O término deve ser igual ou posterior ao início", HttpStatus.BAD_REQUEST);
        }
    }

    private RecorrenciaFinanceiraResponse resposta(RecorrenciaFinanceira item) {
        return new RecorrenciaFinanceiraResponse(
            item.getId(),
            item.getUser().getId(),
            item.getDescricao(),
            item.getTipo(),
            item.getValorPrevisto(),
            item.getFrequencia(),
            item.getInicioEm(),
            item.getFimEm(),
            item.getAtiva()
        );
    }

    @Transactional
    public void ativar(Integer id, Integer userId) {
        RecorrenciaFinanceira item = buscarDoUsuario(id, userId);
        entityManager.refresh(item, LockModeType.PESSIMISTIC_WRITE);
        if (!item.getAtiva()) {
            item.setProcessadaAte(null);
        }
        item.setAtiva(true);
        gerar(item, LocalDate.now());
    }

    @Transactional
    public void desativar(Integer id, Integer userId) {
        RecorrenciaFinanceira item = buscarDoUsuario(id, userId);
        entityManager.refresh(item, LockModeType.PESSIMISTIC_WRITE);
        item.setAtiva(false);
    }

    @Transactional
    public void gerarLancamentos(Integer id, LocalDate hoje) {
        var item = entityManager.find(RecorrenciaFinanceira.class, id, LockModeType.PESSIMISTIC_WRITE);
        if (item != null) {
            gerar(item, hoje);
        }
    }

    private void gerar(RecorrenciaFinanceira item, LocalDate hoje) {
        if (!item.getAtiva()) {
            return;
        }
        LocalDate fim = item.getFimEm() != null && item.getFimEm().isBefore(hoje) ? item.getFimEm() : hoje;
        LocalDate inicio = item.getProcessadaAte() == null ? hoje : item.getProcessadaAte().plusDays(1);
        if (inicio.isBefore(item.getInicioEm())) {
            inicio = item.getInicioEm();
        }
        if (inicio.isAfter(fim)) {
            return;
        }

        CategoriaFinancas categoria = null;
        for (LocalDate data = inicio; !data.isAfter(fim); data = data.plusDays(1)) {
            if (ocorreEm(item, data) && !transacaoRepository.existsByRecorrencia_IdAndCompetencia(item.getId(), data)) {
                if (categoria == null) {
                    categoria = categoriaPadrao(item);
                    entityManager.lock(categoria, LockModeType.PESSIMISTIC_WRITE);
                }
                var lancamento = new TransacaoFinanceira();
                lancamento.setUser(item.getUser());
                lancamento.setCategoria(categoria);
                lancamento.setRecorrencia(item);
                lancamento.setDescricao(item.getDescricao());
                lancamento.setTipo(item.getTipo());
                lancamento.setValor(item.getValorPrevisto());
                lancamento.setCompetencia(data);
                lancamento.setVencimento(data);
                transacaoRepository.save(lancamento);
            }
        }
        item.setProcessadaAte(fim);
    }

    private CategoriaFinancas categoriaPadrao(RecorrenciaFinanceira item) {
        entityManager.lock(item.getUser(), LockModeType.PESSIMISTIC_WRITE);
        return categoriaRepository.findByUser_IdAndNomeIgnoreCase(item.getUser().getId(), "Recorrências").orElseGet(() -> {
            var categoria = new CategoriaFinancas();
            categoria.setUser(item.getUser());
            categoria.setNome("Recorrências");
            return categoriaRepository.save(categoria);
        });
    }

    private boolean ocorreEm(RecorrenciaFinanceira item, LocalDate data) {
        LocalDate inicio = item.getInicioEm();
        return switch (item.getFrequencia()) {
            case "diaria" -> true;
            case "semanal" -> inicio.getDayOfWeek() == data.getDayOfWeek();
            case "mensal" -> Math.min(inicio.getDayOfMonth(), data.lengthOfMonth()) == data.getDayOfMonth();
            case "anual" -> inicio.getMonth() == data.getMonth()
                && Math.min(inicio.getDayOfMonth(), data.lengthOfMonth()) == data.getDayOfMonth();
            default -> false;
        };
    }
}
