package com.iagora.backend.modules.marketplace.publicacaoServico;

import com.iagora.backend.exception.RegraNegocioException;
import com.iagora.backend.modules.comercial.planoPublicacao.PlanoPublicacaoService;
import com.iagora.backend.modules.marketplace.servico.ServicoService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class PublicacaoServicoService {
    private final PublicacaoServicoRepository repository;
    private final ServicoService servicoService;
    private final PlanoPublicacaoService planoService;
    private final EntityManager entityManager;

    public PublicacaoServicoService(PublicacaoServicoRepository repository, ServicoService servicoService,
                                    PlanoPublicacaoService planoService, EntityManager entityManager) {
        this.repository = repository;
        this.servicoService = servicoService;
        this.planoService = planoService;
        this.entityManager = entityManager;
    }

    public PublicacaoServico buscar(Integer id) {
        if (id == null) {
            throw new RegraNegocioException("Informe o identificador", HttpStatus.BAD_REQUEST);
        }
        return repository.findById(id)
            .orElseThrow(() -> new RegraNegocioException("Registro não encontrado", HttpStatus.NOT_FOUND));
    }

    @Transactional
    public PublicacaoServicoResponse cadastrar(Integer servicoId, PublicacaoServicoRequest request, Integer userId) {
        return resposta(criar(servicoId, request.planoId(), userId));
    }

    @Transactional
    public PublicacaoServicoResponse renovar(Integer id, Integer userId) {
        var anterior = buscarDoUsuario(id, userId, false);
        if (!List.of("ativa", "expirada").contains(anterior.getStatus())) {
            throw new RegraNegocioException("Somente uma publicação paga pode ser renovada", HttpStatus.CONFLICT);
        }
        return resposta(criar(anterior.getServico().getId(), anterior.getPlano().getId(), userId));
    }

    @Transactional
    public List<PublicacaoServicoResponse> listar(Integer servicoId, Integer userId) {
        servicoService.buscarDoUsuario(servicoId, userId);
        return repository.findByServico_IdOrderByIdDesc(servicoId).stream().map(this::resposta).toList();
    }

    @Transactional
    public List<PublicacaoServicoResponse> minhas(Integer userId) {
        return repository.findByServico_Empresa_User_IdOrderByIdDesc(userId).stream().map(this::resposta).toList();
    }

    @Transactional
    public PublicacaoServicoResponse consultar(Integer id, Integer userId) {
        return resposta(buscarDoUsuario(id, userId, false));
    }

    @Transactional
    public void cancelar(Integer id, Integer userId) {
        var item = buscarDoUsuario(id, userId, true);
        if (!"pendente".equals(item.getStatus())) {
            throw new RegraNegocioException("Somente publicações pendentes podem ser canceladas", HttpStatus.CONFLICT);
        }
        item.setStatus("cancelada");
    }

    public PublicacaoServico buscarDoUsuario(Integer id, Integer userId, boolean bloquear) {
        var item = buscar(id);
        if (!item.getServico().getEmpresa().getUser().getId().equals(userId)) {
            throw new RegraNegocioException("Publicação pertence a outra empresa", HttpStatus.FORBIDDEN);
        }
        if (bloquear) {
            entityManager.refresh(item, LockModeType.PESSIMISTIC_WRITE);
        }
        return item;
    }

    public void ativarPorPagamento(PublicacaoServico item, LocalDateTime agora) {
        var servico = item.getServico();
        entityManager.refresh(servico, LockModeType.PESSIMISTIC_WRITE);
        if (!"pendente".equals(item.getStatus()) || !servico.getValidado() || !servico.getEmpresa().getValidado()
            || "arquivado".equals(servico.getStatusPublicacao())) {
            throw new RegraNegocioException("Publicação indisponível", HttpStatus.CONFLICT);
        }
        LocalDateTime inicio = servico.getExpiraEm() != null && servico.getExpiraEm().isAfter(agora)
            ? servico.getExpiraEm() : agora;
        item.setInicio(inicio);
        item.setFim(inicio.plusDays(item.getDuracaoDias()));
        item.setStatus("ativa");
        if (inicio.equals(agora)) {
            servico.setPublicadoEm(agora);
        }
        servico.setExpiraEm(item.getFim());
        servico.setStatusPublicacao("publicado");
        servico.setAtivo(true);
    }

    private PublicacaoServico criar(Integer servicoId, Integer planoId, Integer userId) {
        var servico = servicoService.buscarDoUsuario(servicoId, userId);
        entityManager.refresh(servico, LockModeType.PESSIMISTIC_WRITE);
        if (!servico.getValidado() || !servico.getEmpresa().getValidado() || "arquivado".equals(servico.getStatusPublicacao())) {
            throw new RegraNegocioException("O serviço precisa estar aprovado", HttpStatus.CONFLICT);
        }
        if (repository.findByServico_IdOrderByIdDesc(servicoId).stream().anyMatch(item -> "pendente".equals(item.getStatus()))) {
            throw new RegraNegocioException("O serviço já possui uma publicação pendente", HttpStatus.CONFLICT);
        }
        var plano = planoService.buscar(planoId);
        if (!plano.getAtivo()) {
            throw new RegraNegocioException("Plano de publicação inativo", HttpStatus.CONFLICT);
        }
        var item = new PublicacaoServico();
        item.setServico(servico);
        item.setPlano(plano);
        item.setValor(plano.getPreco());
        item.setDuracaoDias(plano.getDuracaoDias());
        return repository.save(item);
    }

    public PublicacaoServicoResponse resposta(PublicacaoServico item) {
        return new PublicacaoServicoResponse(
            item.getId(),
            item.getServico().getId(),
            item.getPlano().getId(),
            item.getInicio(),
            item.getFim(),
            item.getValor(),
            ("ativa".equals(item.getStatus()) && !item.getFim().isAfter(LocalDateTime.now())) ? "expirada" : item.getStatus()
        );
    }
}

