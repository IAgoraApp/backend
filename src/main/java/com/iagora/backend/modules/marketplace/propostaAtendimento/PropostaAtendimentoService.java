package com.iagora.backend.modules.marketplace.propostaAtendimento;

import com.iagora.backend.exception.RegraNegocioException;
import com.iagora.backend.modules.identidade.autenticacao.User;
import com.iagora.backend.modules.marketplace.servicoContratado.ServicoContratadoService;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class PropostaAtendimentoService {
    private final PropostaAtendimentoRepository repository;
    private final ServicoContratadoService contratacaoService;
    private final EntityManager entityManager;

    public PropostaAtendimentoService(PropostaAtendimentoRepository repository,
                                      ServicoContratadoService contratacaoService, EntityManager entityManager) {
        this.repository = repository;
        this.contratacaoService = contratacaoService;
        this.entityManager = entityManager;
    }

    public PropostaAtendimento buscar(Integer id) {
        if (id == null) {
            throw new RegraNegocioException("Informe o identificador", HttpStatus.BAD_REQUEST);
        }
        return repository.findById(id)
            .orElseThrow(() -> new RegraNegocioException("Registro não encontrado", HttpStatus.NOT_FOUND));
    }

    @Transactional
    public List<PropostaAtendimentoResponse> listar(Integer id, Integer userId) {
        var contratacao = contratacaoService.buscarParticipante(id, userId, false);
        contratacaoService.exigirStatus(contratacao, "pendente");
        return repository.findByContratado_IdOrderByIdDesc(id).stream().map(this::resposta).toList();
    }

    @Transactional
    public PropostaAtendimentoResponse cadastrar(Integer id, PropostaAtendimentoRequest request, Integer userId) {
        var contratado = contratacaoService.buscarParticipante(id, userId, true);
        contratacaoService.exigirStatus(contratado, "pendente");
        validarData(request.dataAgendamento());
        if (request.valor() == null || request.valor().signum() < 0 || request.valor().scale() > 2
            || request.valor().precision() - request.valor().scale() > 8) {
            throw new RegraNegocioException("Informe um valor válido", HttpStatus.BAD_REQUEST);
        }
        repository.findByContratado_IdAndStatus(id, "pendente").ifPresent(anterior -> anterior.setStatus("substituida"));
        var proposta = new PropostaAtendimento();
        proposta.setContratado(contratado);
        proposta.setAutorUser(entityManager.getReference(User.class, userId));
        proposta.setDataAgendamento(request.dataAgendamento());
        proposta.setValor(request.valor());
        return resposta(repository.save(proposta));
    }

    @Transactional
    public void confirmar(Integer id, Integer propostaId, Integer userId) {
        var proposta = pendente(id, propostaId, userId, false);
        validarData(proposta.getDataAgendamento());
        var contratado = proposta.getContratado();
        contratado.setDataAgendamento(proposta.getDataAgendamento());
        contratado.setValor(proposta.getValor());
        contratado.setStatus("aceito");
        proposta.setStatus("confirmada");
    }

    @Transactional
    public void recusar(Integer id, Integer propostaId, Integer userId) {
        pendente(id, propostaId, userId, false).setStatus("recusada");
    }

    @Transactional
    public void retirar(Integer id, Integer propostaId, Integer userId) {
        pendente(id, propostaId, userId, true).setStatus("retirada");
    }

    private PropostaAtendimento pendente(Integer id, Integer propostaId, Integer userId, boolean autor) {
        var contratado = contratacaoService.buscarParticipante(id, userId, true);
        contratacaoService.exigirStatus(contratado, "pendente");
        var proposta = buscar(propostaId);
        if (!proposta.getContratado().getId().equals(id)) {
            throw new RegraNegocioException("Proposta não pertence à contratação", HttpStatus.NOT_FOUND);
        }
        if (!"pendente".equals(proposta.getStatus())) {
            throw new RegraNegocioException("Proposta já encerrada", HttpStatus.CONFLICT);
        }
        if (proposta.getAutorUser().getId().equals(userId) != autor) {
            throw new RegraNegocioException("Operação indisponível para este participante", HttpStatus.FORBIDDEN);
        }
        return proposta;
    }

    private void validarData(LocalDateTime data) {
        if (data == null || !data.isAfter(LocalDateTime.now())) {
            throw new RegraNegocioException("Informe um horário futuro", HttpStatus.BAD_REQUEST);
        }
    }

    public PropostaAtendimentoResponse resposta(PropostaAtendimento item) {
        return new PropostaAtendimentoResponse(
            item.getId(),
            item.getContratado().getId(),
            item.getAutorUser().getId(),
            item.getDataAgendamento(),
            item.getValor(),
            item.getStatus()
        );
    }
}

