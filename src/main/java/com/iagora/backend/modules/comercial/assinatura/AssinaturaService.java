package com.iagora.backend.modules.comercial.assinatura;

import com.iagora.backend.exception.RegraNegocioException;
import com.iagora.backend.modules.comercial.pagamento.PagamentoRepository;
import com.iagora.backend.modules.comercial.planoAssinatura.PlanoAssinatura;
import com.iagora.backend.modules.comercial.planoAssinatura.PlanoAssinaturaService;
import com.iagora.backend.modules.identidade.autenticacao.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class AssinaturaService {
    private final AssinaturaRepository repository;
    private final PlanoAssinaturaService planoService;
    private final PagamentoRepository pagamentoRepository;
    private final EntityManager entityManager;

    public AssinaturaService(AssinaturaRepository repository, PlanoAssinaturaService planoService,
                             PagamentoRepository pagamentoRepository, EntityManager entityManager) {
        this.repository = repository;
        this.planoService = planoService;
        this.pagamentoRepository = pagamentoRepository;
        this.entityManager = entityManager;
    }

    public Assinatura buscar(Integer id) {
        if (id == null) {
            throw new RegraNegocioException("Informe o identificador", HttpStatus.BAD_REQUEST);
        }
        return repository.findById(id)
            .orElseThrow(() -> new RegraNegocioException("Registro não encontrado", HttpStatus.NOT_FOUND));
    }

    private static final List<String> ATUAIS = List.of("pendente", "ativa", "atrasada");

    @Transactional
    public AssinaturaResponse cadastrar(AssinaturaRequest request, Integer userId) {
        var user = entityManager.find(User.class, userId, LockModeType.PESSIMISTIC_WRITE);
        if (user == null) {
            throw new RegraNegocioException("Usuário não encontrado", HttpStatus.NOT_FOUND);
        }
        if (repository.findFirstByUser_IdAndStatusInOrderByIdDesc(userId, ATUAIS).isPresent()) {
            throw new RegraNegocioException("Já existe uma assinatura em aberto", HttpStatus.CONFLICT);
        }
        var item = new Assinatura();
        item.setUser(user);
        item.setPlano(planoAtivo(request.planoId()));
        item.setInicio(LocalDateTime.now());
        return resposta(repository.save(item));
    }

    @Transactional
    public AssinaturaResponse minha(Integer userId) {
        return repository.findFirstByUser_IdAndStatusInOrderByIdDesc(userId, ATUAIS).map(this::resposta)
            .orElseThrow(() -> new RegraNegocioException("Assinatura atual não encontrada", HttpStatus.NOT_FOUND));
    }

    @Transactional
    public List<AssinaturaResponse> historico(Integer userId) {
        return repository.findByUser_IdOrderByIdDesc(userId).stream().map(this::resposta).toList();
    }

    @Transactional
    public AssinaturaResponse consultar(Integer id, Integer userId) {
        return resposta(buscarDoUsuario(id, userId, false));
    }

    @Transactional
    public List<AssinaturaResponse> listarTodas() {
        return repository.findAll(Sort.by(Sort.Direction.DESC, "id")).stream().map(this::resposta).toList();
    }

    @Transactional
    public AssinaturaResponse consultarAdmin(Integer id) {
        return resposta(buscar(id));
    }

    @Transactional
    public void cancelar(Integer id, Integer userId) {
        var item = buscarDoUsuario(id, userId, true);
        if (!"cancelada".equals(item.getStatus())) {
            item.setStatus("cancelada");
            item.setCanceladaEm(LocalDateTime.now());
            item.setProximaCobranca(null);
        }
    }

    @Transactional
    public void alterarPlano(Integer id, AssinaturaRequest request, Integer userId) {
        var item = buscarDoUsuario(id, userId, true);
        if (!ATUAIS.contains(item.getStatus())) {
            throw new RegraNegocioException("A assinatura está encerrada", HttpStatus.CONFLICT);
        }
        if (pagamentoRepository.existsByAssinatura_IdAndStatus(id, "pendente")) {
            throw new RegraNegocioException("Cancele a cobrança pendente antes de alterar o plano", HttpStatus.CONFLICT);
        }
        var plano = planoAtivo(request.planoId());
        if (!plano.getId().equals(item.getPlano().getId())) {
            item.setPlano(plano);
            item.setStatus("pendente");
            item.setProximaCobranca(null);
        }
    }

    public Assinatura buscarDoUsuario(Integer id, Integer userId, boolean bloquear) {
        var item = buscar(id);
        if (!item.getUser().getId().equals(userId)) {
            throw new RegraNegocioException("Assinatura pertence a outro usuário", HttpStatus.FORBIDDEN);
        }
        if (bloquear) {
            entityManager.refresh(item, LockModeType.PESSIMISTIC_WRITE);
        }
        return item;
    }

    private PlanoAssinatura planoAtivo(Integer id) {
        var plano = planoService.buscar(id);
        if (!plano.getAtivo()) {
            throw new RegraNegocioException("Plano de assinatura inativo", HttpStatus.CONFLICT);
        }
        return plano;
    }

    public AssinaturaResponse resposta(Assinatura item) {
        return new AssinaturaResponse(
            item.getId(),
            item.getUser().getId(),
            item.getPlano().getId(),
            item.getStatus(),
            item.getInicio(),
            item.getProximaCobranca(),
            item.getCanceladaEm()
        );
    }
}

