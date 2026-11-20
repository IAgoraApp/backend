package com.iagora.backend.modules.marketplace.avaliacaoServico;

import com.iagora.backend.exception.RegraNegocioException;
import com.iagora.backend.modules.marketplace.servico.ServicoService;
import com.iagora.backend.modules.marketplace.servicoContratado.ServicoContratadoService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class AvaliacaoServicoService {
    private final AvaliacaoServicoRepository repository;
    private final ServicoContratadoService contratacaoService;
    private final ServicoService servicoService;
    private final EntityManager entityManager;

    public AvaliacaoServicoService(AvaliacaoServicoRepository repository,
                                   ServicoContratadoService contratacaoService, ServicoService servicoService,
                                   EntityManager entityManager) {
        this.repository = repository;
        this.contratacaoService = contratacaoService;
        this.servicoService = servicoService;
        this.entityManager = entityManager;
    }

    public AvaliacaoServico buscar(Integer id) {
        if (id == null) {
            throw new RegraNegocioException("Informe o identificador", HttpStatus.BAD_REQUEST);
        }
        return repository.findById(id)
            .orElseThrow(() -> new RegraNegocioException("Registro não encontrado", HttpStatus.NOT_FOUND));
    }

    @Transactional
    public AvaliacaoServicoResponse cadastrar(Integer contratacaoId, AvaliacaoServicoRequest request, Integer userId) {
        var contratado = contratacaoService.buscarParticipante(contratacaoId, userId, true);
        contratacaoService.exigirCliente(contratado, userId);
        contratacaoService.exigirStatus(contratado, "concluido");
        if (repository.existsByContratado_Id(contratacaoId)) {
            throw new RegraNegocioException("A contratação já foi avaliada", HttpStatus.CONFLICT);
        }
        var item = new AvaliacaoServico();
        item.setContratado(contratado);
        item.setPerfil(contratado.getPerfil());
        item.setDataAvaliacao(LocalDateTime.now());
        aplicar(item, request);
        repository.saveAndFlush(item);
        recalcularNota(item);
        return resposta(item);
    }

    @Transactional
    public List<AvaliacaoServicoResponse> listar(Integer servicoId, Integer userId) {
        servicoService.buscarAcessivel(servicoId, userId);
        return repository.findByContratado_Servico_IdOrderByIdDesc(servicoId).stream().map(this::resposta).toList();
    }

    @Transactional
    public AvaliacaoServicoResponse consultar(Integer id, Integer userId) {
        var item = buscar(id);
        if (!item.getPerfil().getUser().getId().equals(userId)) {
            servicoService.buscarVisivel(item.getContratado().getServico().getId());
        }
        return resposta(item);
    }

    @Transactional
    public List<AvaliacaoServicoResponse> minhas(Integer userId) {
        return repository.findByPerfil_User_IdOrderByIdDesc(userId).stream().map(this::resposta).toList();
    }

    @Transactional
    public void atualizar(Integer id, AvaliacaoServicoRequest request, Integer userId) {
        var item = buscar(id);
        contratacaoService.exigirCliente(item.getContratado(), userId);
        aplicar(item, request);
        recalcularNota(item);
    }

    @Transactional
    public void apagar(Integer id, Integer userId) {
        var item = buscar(id);
        contratacaoService.exigirCliente(item.getContratado(), userId);
        repository.delete(item);
        repository.flush();
        recalcularNota(item);
    }

    private void recalcularNota(AvaliacaoServico item) {
        var servico = item.getContratado().getServico();
        entityManager.lock(servico, LockModeType.PESSIMISTIC_WRITE);
        var notas = repository.findByContratado_Servico_IdOrderByIdDesc(servico.getId());
        long soma = notas.stream().mapToLong(AvaliacaoServico::getNota).sum();
        var media = notas.isEmpty() ? null : BigDecimal.valueOf(soma)
            .divide(BigDecimal.valueOf(notas.size()), 2, RoundingMode.HALF_UP);
        servico.setNota(media);
    }

    private void aplicar(AvaliacaoServico item, AvaliacaoServicoRequest request) {
        if (request.nota() != null) {
            if (request.nota() < 1 || request.nota() > 5) {
                throw new RegraNegocioException("Campo nota inválido", HttpStatus.BAD_REQUEST);
            }
            item.setNota(request.nota());
        }
        if (item.getNota() == null) {
            throw new RegraNegocioException("Campo nota obrigatório", HttpStatus.BAD_REQUEST);
        }
        if (request.comentario() != null) {
            item.setComentario(request.comentario().strip());
        }
    }

    public AvaliacaoServicoResponse resposta(AvaliacaoServico item) {
        return new AvaliacaoServicoResponse(
            item.getId(),
            item.getContratado().getId(),
            item.getPerfil().getId(),
            item.getNota(),
            item.getComentario(),
            item.getDataAvaliacao()
        );
    }
}

