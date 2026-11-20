package com.iagora.backend.modules.comercial.pagamento;

import com.iagora.backend.exception.RegraNegocioException;
import com.iagora.backend.modules.comercial.assinatura.Assinatura;
import com.iagora.backend.modules.comercial.assinatura.AssinaturaService;
import com.iagora.backend.modules.comercial.metodoPagamento.MetodoPagamentoService;
import com.iagora.backend.modules.identidade.autenticacao.User;
import com.iagora.backend.modules.marketplace.publicacaoServico.PublicacaoServicoService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class PagamentoService {
    private final PagamentoRepository repository;
    private final AssinaturaService assinaturaService;
    private final PublicacaoServicoService publicacaoService;
    private final MetodoPagamentoService metodoService;
    private final EntityManager entityManager;
    private final boolean testeHabilitado;

    public PagamentoService(PagamentoRepository repository, AssinaturaService assinaturaService,
                            PublicacaoServicoService publicacaoService, MetodoPagamentoService metodoService,
                            EntityManager entityManager,
                            @Value("${iagora.pagamentos.teste:false}") boolean testeHabilitado) {
        this.repository = repository;
        this.assinaturaService = assinaturaService;
        this.publicacaoService = publicacaoService;
        this.metodoService = metodoService;
        this.entityManager = entityManager;
        this.testeHabilitado = testeHabilitado;
    }

    public Pagamento buscar(Integer id) {
        if (id == null) {
            throw new RegraNegocioException("Informe o identificador", HttpStatus.BAD_REQUEST);
        }
        return repository.findById(id)
            .orElseThrow(() -> new RegraNegocioException("Registro não encontrado", HttpStatus.NOT_FOUND));
    }

    @Transactional
    public PagamentoResponse cadastrar(PagamentoRequest request, Integer userId) {
        if ((request.assinaturaId() == null) == (request.publicacaoId() == null)) {
            throw new RegraNegocioException("Informe uma assinatura ou uma publicação", HttpStatus.BAD_REQUEST);
        }
        verificarAmbienteTeste();
        var item = new Pagamento();
        item.setUser(entityManager.getReference(User.class, userId));
        item.setCriadoEm(LocalDateTime.now());
        item.setGateway("teste");
        item.setTransacaoExternaId(UUID.randomUUID().toString());
        if (request.metodoId() != null) {
            var metodo = metodoService.buscarDoUsuario(request.metodoId(), userId);
            if (!metodo.getAtivo()) {
                throw new RegraNegocioException("Método de pagamento inativo", HttpStatus.CONFLICT);
            }
            item.setMetodo(metodo);
        }
        if (request.assinaturaId() != null) {
            var assinatura = assinaturaService.buscarDoUsuario(request.assinaturaId(), userId, true);
            if (!podeCobrar(assinatura)
                || repository.existsByAssinatura_IdAndStatus(assinatura.getId(), "pendente")) {
                throw new RegraNegocioException("Assinatura indisponível para nova cobrança", HttpStatus.CONFLICT);
            }
            item.setAssinatura(assinatura);
            item.setValor(assinatura.getPlano().getPreco());
        } else {
            var publicacao = publicacaoService.buscarDoUsuario(request.publicacaoId(), userId, true);
            if (!"pendente".equals(publicacao.getStatus())
                || repository.existsByPublicacao_IdAndStatus(publicacao.getId(), "pendente")) {
                throw new RegraNegocioException("Publicação indisponível para nova cobrança", HttpStatus.CONFLICT);
            }
            item.setPublicacao(publicacao);
            item.setValor(publicacao.getValor());
        }
        return resposta(repository.save(item));
    }

    @Transactional
    public List<PagamentoResponse> listar(Integer userId) {
        return repository.findByUser_IdOrderByIdDesc(userId).stream().map(this::resposta).toList();
    }

    @Transactional
    public PagamentoResponse consultar(Integer id, Integer userId) {
        return resposta(buscarDoUsuario(id, userId));
    }

    @Transactional
    public List<PagamentoResponse> listarTodos() {
        return repository.findAll(Sort.by(Sort.Direction.DESC, "id")).stream().map(this::resposta).toList();
    }

    @Transactional
    public PagamentoResponse consultarAdmin(Integer id) {
        return resposta(buscar(id));
    }

    @Transactional
    public void confirmar(Integer id, Integer userId) {
        verificarAmbienteTeste();
        var item = buscarDoUsuario(id, userId);
        entityManager.refresh(item, LockModeType.PESSIMISTIC_WRITE);
        confirmar(item);
    }

    @Transactional
    public void processarEvento(EventoPagamentoRequest evento) {
        verificarAmbienteTeste();
        if (evento == null || evento.transacaoExternaId() == null || evento.transacaoExternaId().isBlank()
            || evento.transacaoExternaId().length() > 255 || evento.valor() == null
            || (!"pago".equals(evento.status()) && !"cancelado".equals(evento.status()))) {
            throw new RegraNegocioException("Evento de pagamento inválido", HttpStatus.BAD_REQUEST);
        }
        var item = repository.findByTransacaoExternaId(evento.transacaoExternaId())
            .orElseThrow(() -> new RegraNegocioException("Cobrança não encontrada", HttpStatus.NOT_FOUND));
        entityManager.refresh(item, LockModeType.PESSIMISTIC_WRITE);
        if (!"teste".equals(item.getGateway()) || item.getValor().compareTo(evento.valor()) != 0) {
            throw new RegraNegocioException("Evento incompatível com a cobrança", HttpStatus.CONFLICT);
        }
        if ("pago".equals(evento.status())) {
            confirmar(item);
        } else {
            cancelarPendente(item);
        }
    }

    private void confirmar(Pagamento item) {
        if (!"teste".equals(item.getGateway())) {
            throw new RegraNegocioException("Cobrança indisponível para confirmação de teste", HttpStatus.CONFLICT);
        }
        Integer userId = item.getUser().getId();
        if ("pago".equals(item.getStatus())) {
            return;
        }
        exigirPendente(item);
        var agora = LocalDateTime.now();
        if (item.getAssinatura() != null) {
            var assinatura = assinaturaService.buscarDoUsuario(item.getAssinatura().getId(), userId, true);
            if (!podeCobrar(assinatura)) {
                throw new RegraNegocioException("Assinatura indisponível", HttpStatus.CONFLICT);
            }
            assinatura.setStatus("ativa");
            assinatura.setProximaCobranca(agora.plusMonths(1));
        } else {
            var publicacao = publicacaoService.buscarDoUsuario(item.getPublicacao().getId(), userId, true);
            publicacaoService.ativarPorPagamento(publicacao, agora);
        }
        item.setPagoEm(agora);
        item.setStatus("pago");
    }

    @Transactional
    public void cancelar(Integer id, Integer userId) {
        var item = buscarDoUsuario(id, userId);
        entityManager.refresh(item, LockModeType.PESSIMISTIC_WRITE);
        cancelarPendente(item);
    }

    private void cancelarPendente(Pagamento item) {
        if (!"cancelado".equals(item.getStatus())) {
            exigirPendente(item);
            item.setStatus("cancelado");
        }
    }

    private boolean podeCobrar(Assinatura assinatura) {
        return List.of("pendente", "atrasada").contains(assinatura.getStatus())
            || ("ativa".equals(assinatura.getStatus()) && assinatura.getProximaCobranca() != null
                && !assinatura.getProximaCobranca().isAfter(LocalDateTime.now()));
    }

    private Pagamento buscarDoUsuario(Integer id, Integer userId) {
        var item = buscar(id);
        if (!item.getUser().getId().equals(userId)) {
            throw new RegraNegocioException("Pagamento pertence a outro usuário", HttpStatus.FORBIDDEN);
        }
        return item;
    }

    private void exigirPendente(Pagamento item) {
        if (!"pendente".equals(item.getStatus())) {
            throw new RegraNegocioException("Somente pagamentos pendentes aceitam esta operação", HttpStatus.CONFLICT);
        }
    }

    private void verificarAmbienteTeste() {
        if (!testeHabilitado) {
            throw new RegraNegocioException("Pagamentos de teste estão desabilitados", HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    public PagamentoResponse resposta(Pagamento item) {
        return new PagamentoResponse(
            item.getId(),
            item.getUser().getId(),
            item.getMetodo() == null ? null : item.getMetodo().getId(),
            item.getAssinatura() == null ? null : item.getAssinatura().getId(),
            item.getPublicacao() == null ? null : item.getPublicacao().getId(),
            item.getValor(),
            item.getStatus(),
            item.getGateway(),
            item.getTransacaoExternaId(),
            item.getCriadoEm(),
            item.getPagoEm()
        );
    }
}

